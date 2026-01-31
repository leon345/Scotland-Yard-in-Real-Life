package de.leonseeger.scotlandyardinreallife.game.controll

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.leonseeger.scotlandyardinreallife.game.entity.Game
import de.leonseeger.scotlandyardinreallife.game.entity.GameCatalogue
import de.leonseeger.scotlandyardinreallife.game.entity.GameSettings
import de.leonseeger.scotlandyardinreallife.game.entity.GameStatus
import de.leonseeger.scotlandyardinreallife.game.entity.Player
import de.leonseeger.scotlandyardinreallife.game.entity.PlayerCatalogue
import de.leonseeger.scotlandyardinreallife.game.entity.PlayerRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateGameViewModel(
    private val gameCatalogue: GameCatalogue, private val playerCatalogue: PlayerCatalogue
) : ViewModel() {
    private val _gameState = MutableStateFlow<Game?>(null)
    val gamestate: StateFlow<Game?> = _gameState.asStateFlow()

    private val _players =
        MutableStateFlow<List<Player>>(emptyList()) //TODO Backing Property Patter
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentPlayerId = MutableStateFlow<String?>(null)
    val currentPlayerId: StateFlow<String?> = _currentPlayerId.asStateFlow()

    private val _gameSettings = MutableStateFlow(GameSettings.DEFAULT)
    val gameSettings: StateFlow<GameSettings> = _gameSettings.asStateFlow()

    fun createGame(ownerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null


            val owner = Player(
                id = ownerId, currentLocation = null, role = PlayerRole.DETECTIVE
            )

            val newGame = Game(
                id = "",
                createdAt = System.currentTimeMillis(),
                status = GameStatus.WAITING,
                players = listOf(owner),
                owner = owner,
                settings = _gameSettings.value
            )
            val result = withContext(Dispatchers.IO) {
                gameCatalogue.createGame(newGame)
            }
            result.onSuccess { gameId ->
                _currentPlayerId.value = ownerId
                _isLoading.value = false
                observeGame(gameId)
            }.onFailure { e ->
                _error.value = "Fehler beim Erstellen des Spiels: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun joinGame(gameId: String, playerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val player = Player(
                id = playerId, currentLocation = null, role = PlayerRole.DETECTIVE
            )
            val result = withContext(Dispatchers.IO) {
                playerCatalogue.addPlayerToGame(gameId, player)
            }
            result.onSuccess { generatedId ->
                _currentPlayerId.value = generatedId
                _isLoading.value = false
                observeGame(gameId)
            }.onFailure { e ->
                _error.value = "Fehler beim Beitreten: ${e.message}"
                _isLoading.value = false
            }

        }

    }

    fun observeGame(gameId: String) {
        viewModelScope.launch {
            try {
                gameCatalogue.getGameById(gameId).collect { game ->
                    _gameState.value = game
                    if (game == null) {
                        _error.value = "Spiel wurde nicht gefunden"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Fehler beim Laden des Spiels: ${e.message}"
            }

        }

        viewModelScope.launch {
            try {
                playerCatalogue.getPlayersInGame(gameId).collect { players ->
                    _players.value = players ?: emptyList()
                }
            } catch (e: Exception) {
                _error.value = "Fehler beim Laden der Spieler: ${e.message}"
            }

        }

    }

    fun startGame() {
        viewModelScope.launch {
            _gameState.value?.let { game ->
                if (game.players.size < 2) {
                    _error.value = "Mindestens 2 Spieler erforderlich"
                    return@launch
                }
                val result = withContext(Dispatchers.IO) {
                    gameCatalogue.updateGameStatus(game.id, GameStatus.RUNNING)
                }
                result.onFailure { exception ->
                    _error.value = "Fehler beim Starten des Spiels: ${exception.message}"
                }
            }
        }
    }
    fun deleteGame() {
        viewModelScope.launch {
            _gameState.value?.let { game ->
                val result = withContext(Dispatchers.IO) {
                    gameCatalogue.deleteGame(game.id)
                }
                result.onFailure { exception ->
                    _error.value = "Fehler beim Löschen des Spiels: ${exception.message}"
                }
            }
        }
    }

    fun togglePlayerRole(playerId: String) {
        viewModelScope.launch {
            val gameId = _gameState.value?.id ?: return@launch
            val player = _players.value.find { it.id == playerId } ?: return@launch

            val updatedPlayer = player.copy(role = player.role.toggle())

            val result = withContext(Dispatchers.IO) {
                playerCatalogue.updatePlayer(gameId, updatedPlayer)
            }

            result.onFailure { exception ->
                _error.value = "Fehler beim Ändern der Rolle: ${exception.message}"
            }
        }
    }

    fun updateGameSettings(gameDuration: Long, banditRevealInterval: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            _gameSettings.value = GameSettings(
                gameDuration = gameDuration,
                banditRevealInterval = banditRevealInterval
            )

            _gameState.value?.let { curentgame ->
                val updatedGame = curentgame.copy(settings = _gameSettings.value)
                val result = withContext(Dispatchers.IO) {
                    gameCatalogue.updateGame(updatedGame)
                }

                result.onSuccess {
                    _isLoading.value = false
                }.onFailure { exception ->
                    _error.value =
                        "Fehler beim Aktualisieren der Einstellungen: ${exception.message}"
                    _isLoading.value = false
                }
            }

        }

    }


    fun clearError() {
        _error.value = null
    }

}

