package de.leonseeger.scotlandyardinreallife.game.gateway.dto

import android.util.Log
import de.leonseeger.scotlandyardinreallife.game.entity.Game
import de.leonseeger.scotlandyardinreallife.game.entity.GameStatus

data class GameDto(
    val id: String = "",
    val createdAt: Long = 0L,
    val status: String = "",
    //TODO @jannes implemtierung des Spielfeldes
    //val polygon: PolygonDto? = null,
    val players: List<PlayerDto> = emptyList(),
    val owner: PlayerDto? = null,
    val counter: Int = 0
) {
    fun toEntity(): Game = Game(
        id = id,
        createdAt = createdAt,
        status = try {
            GameStatus.valueOf(this.status)
        } catch (e: IllegalArgumentException) {
            Log.e("GameDto", "Invalid GameStatus: $status", e)
            GameStatus.WAITING
        },
        players = players.map { it.toEntity() },
        owner = owner?.toEntity() ?: throw IllegalArgumentException("Owner cannot be null"),
        counter = counter
    )

}

fun Game.toDto(): GameDto = GameDto(
    id = id,
    createdAt = createdAt,
    status = status.name,
    players = players.map { it.toDto() },
    owner = owner.toDto(),
    counter = counter
)
