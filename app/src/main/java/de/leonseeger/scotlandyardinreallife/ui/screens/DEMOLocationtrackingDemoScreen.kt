package de.leonseeger.scotlandyardinreallife.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import de.leonseeger.scotlandyardinreallife.game.controll.LocationService

// ========================================
// 🔧 HIER KANNST DU DEINE IDs HARDCODEN!
// ========================================
private const val DEMO_GAME_ID = "3E3XNLKWuK7TRpLaYuzK"
private const val DEMO_PLAYER_ID = "1"
// ========================================

@Composable
fun LocationServiceDemoScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isServiceRunning by remember { mutableStateOf(false) }
    var hasPermissions by remember {
        mutableStateOf(checkLocationPermissions(context))
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        hasPermissions = allGranted

        if (allGranted) {
            Toast.makeText(context, "✅ Permissions granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "❌ Permissions denied!", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header Icon
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            modifier = Modifier.size(64.dp),
            tint = if (isServiceRunning) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = "GPS Location Service Demo",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isServiceRunning)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Service Status
                Text(
                    text = if (isServiceRunning) "🟢 Service läuft" else "⚪ Service gestoppt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Divider()

                Spacer(modifier = Modifier.height(12.dp))

                // Demo IDs Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Game ID:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = DEMO_GAME_ID,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Player ID:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = DEMO_PLAYER_ID,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main Action Button
        Button(
            onClick = {
                if (!hasPermissions) {
                    requestLocationPermissions(permissionLauncher)
                } else if (isServiceRunning) {
                    stopLocationService(context)
                    isServiceRunning = false
                } else {
                    startLocationService(context, DEMO_GAME_ID, DEMO_PLAYER_ID)
                    isServiceRunning = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isServiceRunning)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (isServiceRunning) Icons.Default.Star else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = when {
                    !hasPermissions -> "📍 Permissions anfordern"
                    isServiceRunning -> "🛑 Service stoppen"
                    else -> "🚀 Service starten"
                },
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Text
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = if (isServiceRunning) {
                    "📍 Deine Position wird alle 5 Sekunden an Firebase gesendet.\n\n" +
                            "📂 Firestore Pfad:\n" +
                            "games/$DEMO_GAME_ID/\n" +
                            "players/$DEMO_PLAYER_ID/\n" +
                            "currentLocation"
                } else {
                    "ℹ️ Starte den Service um GPS-Tracking zu aktivieren.\n\n" +
                            "Location Updates werden in Firestore unter dem angegebenen Pfad gespeichert."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hinweis zum Ändern der IDs
        Text(
            text = "💡 Tipp: IDs oben im Code anpassen!",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

// ========== Helper Functions ==========

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
    launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>
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

private fun startLocationService(context: Context, gameId: String, playerId: String) {
    val serviceIntent = Intent(context, LocationService::class.java).apply {
        putExtra(LocationService.EXTRA_GAME_ID, gameId)
        putExtra(LocationService.EXTRA_PLAYER_ID, playerId)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(serviceIntent)
    } else {
        context.startService(serviceIntent)
    }
}

private fun stopLocationService(context: Context) {
    val serviceIntent = Intent(context, LocationService::class.java)
    context.stopService(serviceIntent)
    Toast.makeText(context, "🛑 Service gestoppt!", Toast.LENGTH_SHORT).show()
}
