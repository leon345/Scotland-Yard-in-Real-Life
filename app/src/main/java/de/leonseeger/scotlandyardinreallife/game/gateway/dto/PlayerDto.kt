package de.leonseeger.scotlandyardinreallife.game.gateway.dto

import android.location.Location
import android.util.Log
import de.leonseeger.scotlandyardinreallife.game.entity.Player
import de.leonseeger.scotlandyardinreallife.game.entity.PlayerRole

data class PlayerDto(
    val id: String = "",
    val currentLocation: LocationDto? = null,
    val role: String = "",
    val inBounds: Boolean = true
) {
    fun toEntity(): Player = Player(
        id = id,
        currentLocation = currentLocation?.toEntity(),
        role = try {
            PlayerRole.valueOf(role)
        } catch (e: IllegalArgumentException) {
            Log.e("PlayerDto", "Invalid PlayerRole: $role", e)
            PlayerRole.DETECTIVE
        },
        inBounds = inBounds
    )
}

fun Player.toDto(): PlayerDto = PlayerDto(
    id = id,
    currentLocation = currentLocation?.toDto(),
    role = role.name,
    inBounds = inBounds
)
