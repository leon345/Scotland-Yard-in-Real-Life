package de.leonseeger.scotlandyardinreallife.game.controll

import de.leonseeger.scotlandyardinreallife.game.entity.Game
import de.leonseeger.scotlandyardinreallife.game.entity.GameCatalogue
import de.leonseeger.scotlandyardinreallife.game.entity.GameStatus
import de.leonseeger.scotlandyardinreallife.game.entity.Player
import de.leonseeger.scotlandyardinreallife.game.entity.PlayerCatalogue
import de.leonseeger.scotlandyardinreallife.game.entity.PlayerRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateGameController(
    private val gameCatalogue: GameCatalogue, private val playerCatalogue: PlayerCatalogue
) {
    private val controllerScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main) // TODO Was macht dies Zeile
    private val _gameState = MutableStateFlow<Game?>(null)
    val gamestate: StateFlow<Game?> = _gameState.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun createGame(ownerId: String) {
        controllerScope.launch {
            _isLoading.value = true
            _error.value = null
        }

        val owner = Player(
            id = ownerId, currentLocation = null, role = PlayerRole.DETECTIVE
        )

        val newGame = Game(
            id = "",
            createdAt = System.currentTimeMillis(),
            status = GameStatus.WAITING,
            players = listOf(owner),
            owner = owner
        )
        gameCatalogue.createGame(newGame).onSuccess { gameId ->
            _isLoading.value = false
            observeGame(gameId)
        }.onFailure { e ->
            _error.value = "Fehler beim Erstellen des Spiels: ${e.message}"
            _isLoading.value = false
        }
    }

    fun observeGame(gameId: String) {
        controllerScope.launch {
            gameCatalogue.getGameById(gameId).collect { game -> _gameState.value = game }
        }
    }

    fun startGame() {
        controllerScope.launch {
            _gameState.value?.let { game ->
                if (game.players.size < 2) {
                    _error.value = "Mindestens 2 Spieler erforderlich"
                    return@launch
                }

                gameCatalogue.updateGameStatus(game.id, GameStatus.RUNNING).onFailure { exception ->
                    _error.value = "Fehler beim Starten des Spiels: ${exception.message}"
                }
            }
        }
    }

    fun addPlayer(playerId: String) {
        controllerScope.launch {
            _gameState.value?.let { game ->
                playerCatalogue.addPlayerToGame(game.id, playerId).onFailure { exception ->
                    _error.value = "Fehler beim Hinzufügen des Spielers: ${exception.message}"
                }

            }
        }
    }

    fun deleteGame() {
        controllerScope.launch {
            _gameState.value?.let { game ->
                gameCatalogue.deleteGame(game.id).onFailure { exception ->
                    _error.value = "Fehler beim Löschen des Spiels: ${exception.message}"
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

}

