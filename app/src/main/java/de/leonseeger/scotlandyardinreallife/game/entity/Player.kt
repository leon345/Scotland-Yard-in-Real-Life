package de.leonseeger.scotlandyardinreallife.game.entity

import android.location.Location

data class Player(
    val id: String,
    val currentLocation: Location?,
    val role: PlayerRole
)
