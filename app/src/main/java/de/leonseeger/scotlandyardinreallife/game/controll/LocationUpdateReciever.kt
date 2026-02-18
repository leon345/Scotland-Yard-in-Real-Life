package de.leonseeger.scotlandyardinreallife.game.controll

import android.location.Location
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object LocationUpdatesBus {

    private val _locationUpdates = MutableSharedFlow<Location>(
        replay = 0,
        extraBufferCapacity = 1
    )

    val locationUpdates = _locationUpdates.asSharedFlow()

    suspend fun emit(location: Location) {
        _locationUpdates.emit(location)
    }
}