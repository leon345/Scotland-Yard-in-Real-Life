package de.leonseeger.scotlandyardinreallife.gateway.dto

import de.leonseeger.scotlandyardinreallife.entity.Player
import de.leonseeger.scotlandyardinreallife.entity.PlayerRole

/**
 * DTO-Entity, die eine [Player]-Instanz für die Firebase-Persistenzschicht serialisiert und mittels [toEntity] in die Entity zurückführt.
 *
 * Dokumentation erstellt mit KI (Perplexity – Claude Sonnet 4.6).
 *
 * @author Leon Seeger
 */
data class PlayerDto(
    val id: String = "",
    val currentLocation: LocationDto? = null,
    val role: String = ""
) {
    fun toEntity(): Player = Player(
        id = id,
        currentLocation = currentLocation?.toEntity(),
        role = try {
            PlayerRole.valueOf(role)
        } catch (e: IllegalArgumentException) {
            PlayerRole.DETECTIVE
        }
    )
}

fun Player.toDto(): PlayerDto = PlayerDto(
    id = id,
    currentLocation = currentLocation?.toDto(),
    role = role.name
)
