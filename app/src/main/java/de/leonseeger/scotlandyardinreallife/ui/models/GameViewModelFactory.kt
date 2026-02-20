package de.leonseeger.scotlandyardinreallife.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.leonseeger.scotlandyardinreallife.entity.GameCatalog
import de.leonseeger.scotlandyardinreallife.entity.PlayerCatalog

/**
 * Factory, die den [GameViewModel] mit den erforderlichen [GameCatalog]- und
 * [PlayerCatalog]-Abhängigkeiten instanziiert.
 *
 * Dokumentation erstellt mit KI (Perplexity – Claude Sonnet 4.6).
 *
 * @author Leon Seeger
 */
class GameViewModelFactory(
    private val gameCatalog: GameCatalog,
    private val playerCatalog: PlayerCatalog
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(gameCatalog, playerCatalog) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}