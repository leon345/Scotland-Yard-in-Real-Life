package de.leonseeger.scotlandyardinreallife.game.entity

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
