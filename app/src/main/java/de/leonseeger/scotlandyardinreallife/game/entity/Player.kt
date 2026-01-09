package de.leonseeger.scotlandyardinreallife.game.entity

import com.google.protobuf.DescriptorProtos

data class Player(
    val id: String,
    val currentLocation: Location?,
    val role: PlayerRole
)
