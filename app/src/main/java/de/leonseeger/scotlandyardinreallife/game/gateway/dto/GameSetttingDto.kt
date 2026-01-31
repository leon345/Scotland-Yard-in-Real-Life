package de.leonseeger.scotlandyardinreallife.game.gateway.dto

import de.leonseeger.scotlandyardinreallife.game.entity.GameSettings

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


