package de.leonseeger.scotlandyardinreallife.game.entity

data class Game(
    val id: String,
    val createdAt: Long,
    val status: GameStatus,
    val polygon: Polygon,
    val players: List<Player>,
    val owner: Player
) {


}