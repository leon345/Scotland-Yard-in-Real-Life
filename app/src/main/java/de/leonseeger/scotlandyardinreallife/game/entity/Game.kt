package de.leonseeger.scotlandyardinreallife.game.entity

import org.maplibre.geojson.Point

data class Game(
    val id: String,
    val createdAt: Long,
    val status: GameStatus,
    val polygon: List<Point>,
    val players: List<Player>,
    val owner: Player,
    val counter: Int = 0,
    val settings: GameSettings
) {


}