package de.leonseeger.scotlandyardinreallife.game.gateway.dto

import android.location.Location

data class LocationDto(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
) {
    fun toEntity(): Location = Location("").apply {
        latitude = this@LocationDto.latitude
        longitude = this@LocationDto.longitude
    }
}

fun Location.toDto(): LocationDto = LocationDto(
    latitude = latitude,
    longitude = longitude
)

