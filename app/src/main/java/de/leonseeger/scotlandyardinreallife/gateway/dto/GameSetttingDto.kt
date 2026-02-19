package de.leonseeger.scotlandyardinreallife.gateway.dto

import de.leonseeger.scotlandyardinreallife.entity.GameSettings

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


