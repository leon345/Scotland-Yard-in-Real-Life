package de.leonseeger.scotlandyardinreallife.game.gateway.locationHelper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


class LocationRepository(
    private val context: Context
) {
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(this.context)

    fun checkLocationPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation && coarseLocation
    }

    suspend fun getLastKnownLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            if (!checkLocationPermissions()) {
                cont.resume(null)
                return@suspendCancellableCoroutine
            }

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    cont.resume(location)
                }
                .addOnFailureListener {
                    if (cont.isActive)
                        cont.resume(null)
                }
        }

    suspend fun getCurrentLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            if (!checkLocationPermissions()) {
                cont.resume(null)
                return@suspendCancellableCoroutine
            }

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    cont.resume(location)
                }
                .addOnFailureListener {
                    if (cont.isActive)
                        cont.resume(null)
                }
        }
}