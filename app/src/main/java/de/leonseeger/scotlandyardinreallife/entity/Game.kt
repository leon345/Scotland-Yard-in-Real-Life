package de.leonseeger.scotlandyardinreallife.entity

import org.maplibre.geojson.Point

/**
 * Entity-Objekt, das ein aktives Scotland-Yard-Spiel repräsentiert.
 *
 * Dokumentation erstellt mit KI (Perplexity – Claude Sonnet 4.6).
 *
 * @author Leon Seeger & Jannes Schophuis
 */
data class Game(
    val id: String,
    val createdAt: Long,
    val status: GameStatus,
    val polygon: List<Point>,
    val players: List<Player>,
    val owner: Player,
    val counter: Int = 0,
    val settings: GameSettings,
    var gameWinner: PlayerRole?
) {


}