package de.leonseeger.scotlandyardinreallife.gateway.dto

import android.util.Log
import de.leonseeger.scotlandyardinreallife.entity.Player
import de.leonseeger.scotlandyardinreallife.entity.PlayerRole

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
            Log.e("PlayerDto", "Invalid PlayerRole: $role", e)
            PlayerRole.DETECTIVE
        }
    )
}

fun Player.toDto(): PlayerDto = PlayerDto(
    id = id,
    currentLocation = currentLocation?.toDto(),
    role = role.name
)
