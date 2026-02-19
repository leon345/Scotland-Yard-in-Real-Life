package de.leonseeger.scotlandyardinreallife.gateway.dto

import de.leonseeger.scotlandyardinreallife.entity.GameSettings

/**
 * DTO-Entity, die eine [GameSettings]-Instanz für die Firebase-Persistenzschicht serialisiert und mittels [toEntity] in die Entity zurückführt.
 *
 * Dokumentation erstellt mit KI (Perplexity – Claude Sonnet 4.6).
 *
 * @author Leon Seeger
 */
data class GameSettingDto(
    val gameDuration: Long = 60,
    val banditRevealInterval: Long = 5,
) {
    fun toEntity(): GameSettings = GameSettings(
        gameDuration = gameDuration,
        banditRevealInterval = banditRevealInterval
    )
}

fun GameSettings.toDto(): GameSettingDto = GameSettingDto(
    gameDuration = gameDuration,
    banditRevealInterval = banditRevealInterval
)


