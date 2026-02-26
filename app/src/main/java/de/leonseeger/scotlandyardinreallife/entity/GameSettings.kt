package de.leonseeger.scotlandyardinreallife.entity

import de.leonseeger.scotlandyardinreallife.entity.GameSettings.Companion.DEFAULT


/**
 * Entity, die die Konfigurationsparameter eines [Game] kapselt und Standardwerte über [DEFAULT] bereitstellt.
 *
 * Dokumentation erstellt mit KI (Perplexity – Claude Sonnet 4.6).
 *
 * @author Leon Seeger
 */
data class GameSettings(
    val gameDuration: Long,
    val banditRevealInterval: Long,
) {
    companion object {
        val DEFAULT = GameSettings(
            gameDuration = 60L,
            banditRevealInterval = 5L
        )
    }
}
