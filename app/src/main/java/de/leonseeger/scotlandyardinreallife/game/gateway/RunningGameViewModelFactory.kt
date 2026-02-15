package de.leonseeger.scotlandyardinreallife.game.gateway

import RunningGameViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RunningGameViewModelFactory(
    private val gameId: String,
    private val playerId: String,
    private val firebaseGateway: FirebaseGateway
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RunningGameViewModel(
            gameId = gameId,
            playerId = playerId,
            gameCatalogue = firebaseGateway
        ) as T
    }
}