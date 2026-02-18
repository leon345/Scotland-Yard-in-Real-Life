package de.leonseeger.scotlandyardinreallife.game.controll

import android.content.Context
import android.location.Location
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
import org.maplibre.android.geometry.LatLng
import org.maplibre.geojson.Point
import org.maplibre.geojson.Polygon
import org.maplibre.turf.TurfJoins


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

    private val _playArea = MutableStateFlow<List<Point>>(mutableListOf<Point>())
    val playArea: StateFlow<List<Point>> = _playArea.asStateFlow()

    fun createGame(ownerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null


            val owner = Player(
                id = ownerId, currentLocation = null, role = PlayerRole.DETECTIVE, inBounds = true
            )

            val newGame = Game(
                id = "",
                createdAt = System.currentTimeMillis(),
                status = GameStatus.WAITING,
                players = listOf(owner),
                owner = owner,
                settings = _gameSettings.value,
                polygon = playArea.value
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
                id = playerId, currentLocation = null, role = PlayerRole.DETECTIVE, true
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

    fun getCurrPlayerRole(): PlayerRole{
        val currPlayer = players.value.find { player -> player.id == currentPlayerId.value }
        if(currPlayer != null)
            return currPlayer.role
        return PlayerRole.DETECTIVE
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

    fun endGame(){
        viewModelScope.launch {
            _gameState.value?.let{ game ->
                val result = withContext(Dispatchers.IO) {
                    gameCatalogue.updateGameStatus(game.id, GameStatus.FINISHED)
                }
                result.onFailure { exception ->
                    _error.value = "Konnte Spiel nicht beenden: ${exception.message}"
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

    fun setPlayerInBounds(playerId: String, inBounds: Boolean){
        viewModelScope.launch {
            val gameId = _gameState.value?.id ?: return@launch
            val player = _players.value.find { it.id == playerId } ?: return@launch

            val updatedPlayer = player.copy(inBounds = inBounds)
            val result = withContext(Dispatchers.IO) {
                playerCatalogue.updatePlayer(gameId, updatedPlayer)
            }

            result.onFailure { exception ->
                _error.value = "Fehler beim Ändern des InBounds status: ${exception.message}"
            }
        }
    }

    /**
     * Starts the Location service and primes the Update interval based on the player role.
     * Registers a callback to determine if player is in bounds
     */
    fun startLocationServices(serviceContext: Context){
        val gameId = gamestate.value?.id ?: return
        val playerId = currentPlayerId.value ?: return
        startLocationService(serviceContext, gameId, playerId, getCurrPlayerRole())
        viewModelScope.launch {
            LocationUpdatesBus.locationUpdates.collect { location ->
                handleLocationUpdate(location)
            }
        }
    }

    private fun handleLocationUpdate(location: Location) {
        val currentPlayerId = _currentPlayerId.value ?: return
        val game = _gameState.value ?: return

        val isInside = isPointInsidePolygon(LatLng(latitude = location.latitude, longitude = location.longitude),
            game.polygon)

        setPlayerInBounds(currentPlayerId, isInside)
    }

    fun isPointInsidePolygon(
        pos: LatLng,
        polygon: List<Point>
    ): Boolean {

        val point = Point.fromLngLat(pos.longitude, pos.latitude)

        val polygon = Polygon.fromLngLats(listOf<List<Point>>(polygon))

        return TurfJoins.inside(point, polygon)
    }

    fun stopLocationServices(serviceContext: Context){
        stopLocationService(serviceContext)
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

    fun setPlayArea(poly: List<Point>) {
        _playArea.value = poly
    }
}

