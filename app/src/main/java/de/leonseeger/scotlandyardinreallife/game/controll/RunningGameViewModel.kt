import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.leonseeger.scotlandyardinreallife.game.entity.Game
import de.leonseeger.scotlandyardinreallife.game.entity.GameCatalogue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RunningGameViewModel(
    private val gameId: String,
    private val playerId: String,
    private val gameCatalogue: GameCatalogue
) : ViewModel() {

    private val _gameState = MutableStateFlow<Game?>(null)
    val gamestate: StateFlow<Game?> = _gameState.asStateFlow()

    init {
        observeGame()
    }

    private fun observeGame() {
        viewModelScope.launch {
            gameCatalogue.getGameById(gameId).collect {
                _gameState.value = it
            }
        }
    }
}