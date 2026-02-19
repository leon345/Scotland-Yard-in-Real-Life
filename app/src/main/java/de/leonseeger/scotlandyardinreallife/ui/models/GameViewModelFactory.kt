package de.leonseeger.scotlandyardinreallife.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.leonseeger.scotlandyardinreallife.entity.GameCatalogue
import de.leonseeger.scotlandyardinreallife.entity.PlayerCatalogue

class GameViewModelFactory(
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