package de.leonseeger.scotlandyardinreallife.controll

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
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
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
import de.leonseeger.scotlandyardinreallife.entity.Player
import de.leonseeger.scotlandyardinreallife.entity.PlayerCatalog
import de.leonseeger.scotlandyardinreallife.entity.PlayerRole
import de.leonseeger.scotlandyardinreallife.gateway.FirebaseGateway
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Foreground-Service zur kontinuierlichen GPS-Standorterfassung und Echtzeit-Synchronisation
 *
 * Dokumentation erstellt mit KI (Perplexity – Claude Sonnet 4.6).
 *
 * @author Jannes Schophuis & Leon Seeger
 */
class LocationService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var playerCatalog: PlayerCatalog

    private var dispatcher: CoroutineDispatcher = Dispatchers.IO
    private lateinit var serviceScope: CoroutineScope
    private lateinit var locationCallback: LocationCallback

    private var gameId: String? = null
    private var playerId: String? = null
    private var updateDelay: Long = 5000L


    companion object {
        private const val TAG = "LocationService"
        const val EXTRA_GAME_ID = "EXTRA_GAME_ID"
        const val EXTRA_PLAYER_ID = "EXTRA_PLAYER_ID"
        const val EXTRA_UPDATE_DELAY = "EXTRA_UPDATE_DELAY"

        private const val NOTIFICATION_CHANNEL_ID = "location_tracking_channel"
        private const val NOTIFICATION_ID = 1001


    }

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(SupervisorJob() + dispatcher)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        playerCatalog = FirebaseGateway(FirebaseFirestore.getInstance())
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
        updateDelay = intent.getLongExtra(EXTRA_UPDATE_DELAY, 5000L)
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
            val importance = NotificationManager.IMPORTANCE_LOW
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
                val currenPlayer: Player? = playerCatalog.getPlayer(gameId, playerId).first()
                if (currenPlayer == null) {
                    Log.e(TAG, "Player not found in database: $playerId")
                    return@launch
                }
                val updatedPlayer = currenPlayer.copy(currentLocation = location)
                val result = playerCatalog.updatePlayer(gameId, updatedPlayer)
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
            LocationRequest.Builder(updateDelay)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(updateDelay-300)
                .setMaxUpdateDelayMillis(updateDelay+700).build()


        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
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
                this, Manifest.permission.ACCESS_FINE_LOCATION
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

fun startLocationService(context: Context, gameId: String, playerId: String, playerRole: PlayerRole) {
    val updateInterval: Long = if(playerRole == PlayerRole.DETECTIVE) 1000L else 20000L

    val serviceIntent = Intent(context, LocationService::class.java).apply {
        putExtra(LocationService.EXTRA_GAME_ID, gameId)
        putExtra(LocationService.EXTRA_PLAYER_ID, playerId)
        putExtra(LocationService.EXTRA_UPDATE_DELAY, updateInterval)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(serviceIntent)
    } else {
        context.startService(serviceIntent)
    }
}

fun startLocationService(context: Context, gameId: String, playerId: String, customDelay: Long) {
    val serviceIntent = Intent(context, LocationService::class.java).apply {
        putExtra(LocationService.EXTRA_GAME_ID, gameId)
        putExtra(LocationService.EXTRA_PLAYER_ID, playerId)
        putExtra(LocationService.EXTRA_UPDATE_DELAY, customDelay)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(serviceIntent)
    } else {
        context.startService(serviceIntent)
    }
}

fun stopLocationService(context: Context) {
    val serviceIntent = Intent(context, LocationService::class.java)
    context.stopService(serviceIntent)
}

private fun checkLocationPermissions(context: Context): Boolean {
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

private fun requestLocationPermissions(
    launcher: ActivityResultLauncher<Array<String>>
) {
    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Android 13+ Notification Permission
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    launcher.launch(permissions.toTypedArray())
}

