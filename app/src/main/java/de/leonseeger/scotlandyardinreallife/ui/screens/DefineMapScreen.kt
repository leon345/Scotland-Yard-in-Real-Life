package de.leonseeger.scotlandyardinreallife.ui.screens

import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.leonseeger.scotlandyardinreallife.game.controll.DefinePlaymapViewModel
import de.leonseeger.scotlandyardinreallife.game.controll.LocationPermissionState
import de.leonseeger.scotlandyardinreallife.ui.component.CenteredLoadingIndicator
import de.leonseeger.scotlandyardinreallife.ui.component.gamemap.PlayMap
import de.leonseeger.scotlandyardinreallife.ui.component.gamemap.PlayMapData
import org.maplibre.android.geometry.LatLng

@Composable
fun DefineMapScreen(
    viewModel: DefinePlaymapViewModel = viewModel()
) {

    val permissionGranted by viewModel.permissionGranted.collectAsState()

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            viewModel.onPermissionResult(granted)
        }

    when (permissionGranted) {
        LocationPermissionState.Granted -> {
            DefiningMap(viewModel)
        }
        LocationPermissionState.RequestRequired -> {
            LaunchedEffect(Unit) {
                permissionLauncher.launch(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
        LocationPermissionState.Denied -> {
            PermissionDeniedScreen()
        }
    }
}


@Composable
fun DefiningMap(viewModel: DefinePlaymapViewModel) {
    val location by viewModel.currentLocation.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCurrLocation()
    }

    when (location) {
        null -> CenteredLoadingIndicator()
        else -> {
            val playMapData = remember { PlayMapData() }
            PlayareaDefinitionMap(viewModel.getContext(), playMapData, location!!)
        }
    }

    Button(onClick = { viewModel.loadCurrLocation() },
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 60.dp)) {
        Text("Reload")
    }
}

@Composable
fun PlayareaDefinitionMap(
    context: Context,
    mapData: PlayMapData,
    startLocation: Location,
) {
    mapData.CustomMap(
        modifier = Modifier.fillMaxSize(),
        lat = startLocation.latitude,
        lon = startLocation.longitude,
        appContext = context,
        onMapReady = { map ->
            map.getMapLibreMap().addOnMapClickListener { point ->
                if (!map.addPolyPoint(point)) {
                    Toast.makeText(context, "Polygon braucht mehr Punkte", Toast.LENGTH_SHORT)
                        .show()
                }
                true
            }
        }
    )
}
