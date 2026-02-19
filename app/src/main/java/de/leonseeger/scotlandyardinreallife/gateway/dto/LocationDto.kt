package de.leonseeger.scotlandyardinreallife.gateway.dto

import android.location.Location
import org.maplibre.geojson.Point

data class LocationDto(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
) {
    fun toEntity(): Location = Location("").apply {
        latitude = this@LocationDto.latitude
        longitude = this@LocationDto.longitude
    }

    fun toPointEntity(): Point{
        return Point.fromLngLat(this@LocationDto.longitude, this@LocationDto.latitude)
    }
}

fun Location.toDto(): LocationDto = LocationDto(
    latitude = latitude,
    longitude = longitude
)

fun Point.toDto(): LocationDto = LocationDto(
    latitude = latitude(),
    longitude = longitude()
)

