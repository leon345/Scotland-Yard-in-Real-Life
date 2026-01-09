package de.leonseeger.scotlandyardinreallife.game.entity

import android.location.Location
import com.google.protobuf.DescriptorProtos

data class Player(
    val id: String,
    val currentLocation: Location?,
    val role: PlayerRole
)
