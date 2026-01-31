package de.leonseeger.scotlandyardinreallife.game.entity

data class Game(
    val id: String,
    val createdAt: Long,
    val status: GameStatus,
    //TODO @jannes implemtierung des Spielfeldes
    //val polygon: Polygon,
    val players: List<Player>,
    val owner: Player,
    val counter: Int = 0,
    val settings: GameSettings
) {


}