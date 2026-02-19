package de.leonseeger.scotlandyardinreallife.gateway.dto

import de.leonseeger.scotlandyardinreallife.entity.Game
import de.leonseeger.scotlandyardinreallife.entity.GameStatus
import de.leonseeger.scotlandyardinreallife.entity.PlayerRole

/**
 * DTO-Entity, die eine [Game]-Instanz für die Firebase-Persistenzschicht serialisiert und mittels [toEntity] in die Entity zurückführt.
 *
 * Dokumentation erstellt mit KI (Perplexity – Claude Sonnet 4.6).
 *
 * @author Leon Seeger & Jannes Schophuis
 */
data class GameDto(
    val id: String = "",
    val createdAt: Long = 0L,
    val status: String = "",
    val polygon: List<LocationDto> = emptyList(),
    val players: List<PlayerDto> = emptyList(),
    val owner: PlayerDto? = null,
    val counter: Int = 0,
    val settings: GameSettingDto? = null,
    val gameWinner: PlayerRole? = null
) {
    fun toEntity(): Game = Game(
        id = id,
        createdAt = createdAt,
        status = try {
            GameStatus.valueOf(this.status)
        } catch (e: IllegalArgumentException) {
            GameStatus.WAITING
        },
        players = players.map { it.toEntity() },
        owner = owner?.toEntity() ?: throw IllegalArgumentException("Owner cannot be null"),
        counter = counter,
        polygon = polygon.map { it.toPointEntity() },
        settings = settings?.toEntity() ?: throw IllegalArgumentException("Settings cannot be null"),
        gameWinner = gameWinner
    )

}

fun Game.toDto(): GameDto = GameDto(
    id = id,
    createdAt = createdAt,
    status = status.name,
    players = players.map { it.toDto() },
    owner = owner.toDto(),
    counter = counter,
    settings = settings.toDto(),
    polygon = polygon.map { it.toDto() },
    gameWinner = gameWinner
)
