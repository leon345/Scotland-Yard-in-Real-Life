package de.leonseeger.scotlandyardinreallife.controll

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Stellt den aktuellen GPS-Standort des Geräts bereit und abstrahiert den Zugriff
 * auf den [com.google.android.gms.location.FusedLocationProviderClient] von Google Play Services.
 * Dokumentation erstellt mit KI (Perplexity – Claude Sonnet 4.6).
 * @author TODO Author
 */

class LocationProvider(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermissions(): Boolean {
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

    suspend fun getCurrentLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            try {
                if (!hasLocationPermissions()) {
                    cont.resume(null)
                    return@suspendCancellableCoroutine
                }

                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc -> cont.resume(loc) }
                    .addOnFailureListener { e ->
                        if (cont.isActive) cont.resume(null)
                    }

            } catch (se: SecurityException) {
                Log.w("LocationService", "Location permission revoked", se)
                if (cont.isActive) cont.resume(null)
            }
        }
}