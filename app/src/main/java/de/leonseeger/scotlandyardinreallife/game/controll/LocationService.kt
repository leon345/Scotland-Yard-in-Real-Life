package de.leonseeger.scotlandyardinreallife.game.controll

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.FirebaseFirestore
import de.leonseeger.scotlandyardinreallife.MainActivity
import de.leonseeger.scotlandyardinreallife.R
import de.leonseeger.scotlandyardinreallife.game.controll.LocationService.Companion.TAG
import de.leonseeger.scotlandyardinreallife.game.entity.Player
import de.leonseeger.scotlandyardinreallife.game.entity.PlayerCatalogue
import de.leonseeger.scotlandyardinreallife.game.gateway.FirebaseGateway
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var playerCatalogue: PlayerCatalogue

    private var dispatcher: CoroutineDispatcher = Dispatchers.IO
    private lateinit var serviceScope: CoroutineScope
    private lateinit var locationCallback: LocationCallback

    private var gameId: String? = null
    private var playerId: String? = null

    companion object {
        private const val TAG = "LocationService"
        const val EXTRA_GAME_ID = "EXTRA_GAME_ID"
        const val EXTRA_PLAYER_ID = "EXTRA_PLAYER_ID"
        private const val NOTIFICATION_CHANNEL_ID = "location_tracking_channel"
        private const val NOTIFICATION_ID = 1001
        private const val LOCATION_UPDATE_INTERVAL_MS = 5000L
        private const val MIN_UPDATE_INTERVAL_MS = 3000L
        private const val MAX_UPDATE_DELAY_MS = 10000L
    }

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(SupervisorJob() + dispatcher)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        playerCatalogue = FirebaseGateway(FirebaseFirestore.getInstance())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation ?: return
                val currentGameId = gameId ?: return
                val currentPlayerId = playerId ?: return
                getAndUpdatePlayer(currentGameId, currentPlayerId, location)

            }


        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            Log.e("LocationService", "Intent is null")
            stopSelf()
            return START_NOT_STICKY
        }
        gameId = intent.getStringExtra(EXTRA_GAME_ID)
        playerId = intent.getStringExtra(EXTRA_PLAYER_ID)
        if (gameId == null || playerId == null) {
            Log.e("LocationService", "gameId or playerId is null")
            stopSelf()
            return START_NOT_STICKY
        }
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        startLocationUpdates()


        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.location_tracking_channel_name)
            val descriptionText = getString(R.string.location_tracking_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.location_tracking_notification_title))
            .setContentText(getString(R.string.location_tracking_notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent) //TODO App Logo
            .setOngoing(true).setPriority(
                NotificationCompat.PRIORITY_LOW
            ).build()
    }

    private fun getAndUpdatePlayer(gameId: String, playerId: String, location: Location) {
        serviceScope.launch {
            try {
                val currenPlayer: Player? = playerCatalogue.getPlayer(gameId, playerId).first()
                if (currenPlayer == null) {
                    Log.e(TAG, "Player not found in database: $playerId")
                    return@launch
                }
                val updatedPlayer = currenPlayer.copy(currentLocation = location)
                val result = playerCatalogue.updatePlayer(gameId, updatedPlayer)
                result.onFailure {
                    Log.e(TAG, "Failed to update player: $updatedPlayer", it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating player location", e)
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest =
            LocationRequest.Builder(LOCATION_UPDATE_INTERVAL_MS)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MS)
                .setMaxUpdateDelayMillis(MAX_UPDATE_DELAY_MS).build()


        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Location permission not granted! Stopping service.")
            stopSelf()
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
            .addOnFailureListener { e ->
                Log.e(TAG, "Error starting location updates", e)
                stopSelf()
            }


    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error stopping location updates", exception)
            }
    }

    fun getLastKnownLocation(onResult: (Location?) -> Void){
        if(ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED){
            Log.e(TAG, "Location permission not granted")
            onResult(null)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                onResult(location)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get last known location", e)
                onResult(null)
            }
    }
}

