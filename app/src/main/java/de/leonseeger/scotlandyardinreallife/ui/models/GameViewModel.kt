package de.leonseeger.scotlandyardinreallife.ui.models

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.leonseeger.scotlandyardinreallife.controll.startLocationService
import de.leonseeger.scotlandyardinreallife.controll.stopLocationService
import de.leonseeger.scotlandyardinreallife.entity.Game
import de.leonseeger.scotlandyardinreallife.entity.GameCatalogue
import de.leonseeger.scotlandyardinreallife.entity.GameSettings
import de.leonseeger.scotlandyardinreallife.entity.GameStatus
import de.leonseeger.scotlandyardinreallife.entity.Player
import de.leonseeger.scotlandyardinreallife.entity.PlayerCatalogue
import de.leonseeger.scotlandyardinreallife.entity.PlayerRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.geojson.Point
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.maplibre.android.geometry.LatLng
import org.maplibre.geojson.Polygon
import org.maplibre.turf.TurfJoins

class CreateGameViewModel(
    private val gameCatalogue: GameCatalogue, private val playerCatalogue: PlayerCatalogue
) : ViewModel() {
    private val _gameState = MutableStateFlow<Game?>(null)
    val gamestate: StateFlow<Game?> = _gameState.asStateFlow()

    private val _players =
        MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentPlayerId = MutableStateFlow<String?>(null)
    val currentPlayerId: StateFlow<String?> = _currentPlayerId.asStateFlow()

    private val _gameSettings = MutableStateFlow(GameSettings.Companion.DEFAULT)
    val gameSettings: StateFlow<GameSettings> = _gameSettings.asStateFlow()

    private val _playArea = MutableStateFlow<List<Point>>(mutableListOf<Point>())
    val playArea: StateFlow<List<Point>> = _playArea.asStateFlow()

    private var gameTimerJob: Job? = null

    private lateinit var serviceContext: Context
    var inBounds = true

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
                settings = _gameSettings.value,
                polygon = playArea.value,
                gameWinner = null
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

    fun getCurrPlayerRole(): PlayerRole {
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
                    if (game?.status == GameStatus.RUNNING) {
                        startGameTimer()
                    }

                    if (game?.status == GameStatus.FINISHED) {
                        gameTimerJob?.cancel()
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
                    //faster location updates when out of bounds
                    val currPlayer = _players.value.find { it.id == _currentPlayerId.value }
                    if(currPlayer?.currentLocation != null){
                        val playerLoc = LatLng(currPlayer.currentLocation.latitude, currPlayer.currentLocation.longitude)
                        testInBoundsChanged(isPointInsidePolygon(playerLoc, gamestate.value?.polygon))
                    }
                }
            } catch (e: Exception) {
                _error.value = "Fehler beim Laden der Spieler: ${e.message}"
            }

        }
    }

    /**
     * only change location update speed when changing state of inBounds
     */
    fun testInBoundsChanged(currentInBounds: Boolean){
        if(currentInBounds != inBounds){
            changeLocationUpdateSpeed(currentInBounds)
            inBounds = currentInBounds
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

    private fun startGameTimer() {
        gameTimerJob?.cancel()

        val durationMillis = gameSettings.value.gameDuration * 60_000L

        gameTimerJob = viewModelScope.launch {
            delay(durationMillis)
            endGame(PlayerRole.BANDIT)
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

    fun endGame(winnerTeam: PlayerRole){
        gameTimerJob?.cancel()

        viewModelScope.launch {
            _gameState.value?.let{ game ->
                val result = withContext(Dispatchers.IO) {
                    _gameState.value?.gameWinner = winnerTeam
                    gameCatalogue.endGame(game.id, winnerTeam)
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

    /**
     * Starts the Location service and primes the Update interval based on the player role.
     * Registers a callback to determine if player is in bounds
     */
    fun startLocationServices(serviceContext: Context){
        val gameId = gamestate.value?.id ?: return
        val playerId = currentPlayerId.value ?: return
        this.serviceContext = serviceContext
        val updateInterval: Long = if(getCurrPlayerRole() == PlayerRole.DETECTIVE) 1000L else gameSettings.value.banditRevealInterval * 60000
        Log.d("Update interval", "" + gameSettings.value.banditRevealInterval)
        startLocationService(serviceContext, gameId, playerId, updateInterval)
    }

    fun stopLocationServices(serviceContext: Context){
        stopLocationService(serviceContext)
    }

    /**
     * Stops location Service and restarts with new interval
     */
    fun changeLocationUpdateSpeed(resetToNormalSpeed: Boolean){
        stopLocationServices(serviceContext)
        if(resetToNormalSpeed)
            startLocationServices(serviceContext)
        else{
            val gameId = gamestate.value?.id ?: return
            val playerId = currentPlayerId.value ?: return
            startLocationService(serviceContext, gameId, playerId, 600L)
        }

    }

    fun clearError() {
        _error.value = null
    }

    fun setPlayArea(poly: List<Point>) {
        _playArea.value = poly
    }
}

fun isPointInsidePolygon(
    pos: LatLng,
    polygon: List<Point>?
): Boolean {
    if (polygon == null) {
        Log.w("Player Bounds", "Polygon not defined")
        return false
    }

    val point = Point.fromLngLat(pos.longitude, pos.latitude)

    val polygon = Polygon.fromLngLats(listOf<List<Point>>(polygon))

    return TurfJoins.inside(point, polygon)
}

