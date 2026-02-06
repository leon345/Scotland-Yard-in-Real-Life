package de.leonseeger.scotlandyardinreallife.game.controll

import androidx.lifecycle.ViewModel
import de.leonseeger.scotlandyardinreallife.game.entity.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class RunningGameViewModel( gameState: Game, currentPlayerId: String): ViewModel() {
    private val _gameState = MutableStateFlow<Game?>(gameState)
    val gamestate = _gameState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentPlayerId = MutableStateFlow(currentPlayerId)
    var currentPlayerId = _currentPlayerId.asStateFlow()


}