package de.leonseeger.scotlandyardinreallife.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.leonseeger.scotlandyardinreallife.game.controll.CreateGameViewModel
import de.leonseeger.scotlandyardinreallife.game.entity.GameCatalogue
import de.leonseeger.scotlandyardinreallife.game.entity.PlayerCatalogue

class CreateGameViewModelFactory(
    private val gameCatalogue: GameCatalogue,
    private val playerCatalogue: PlayerCatalogue
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateGameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateGameViewModel(gameCatalogue, playerCatalogue) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}