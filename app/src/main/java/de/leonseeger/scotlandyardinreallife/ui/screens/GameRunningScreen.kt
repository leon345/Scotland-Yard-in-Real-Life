package de.leonseeger.scotlandyardinreallife.ui.screens

import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.leonseeger.scotlandyardinreallife.game.controll.LocationPermissionState
import de.leonseeger.scotlandyardinreallife.game.controll.MapLocationViewModel
import de.leonseeger.scotlandyardinreallife.game.controll.RunningGameViewModel
import de.leonseeger.scotlandyardinreallife.ui.component.CenteredLoadingIndicator
import de.leonseeger.scotlandyardinreallife.ui.component.gamemap.PlayMapData
import org.maplibre.android.geometry.LatLng

/**
 * The Map shown before the Game Lobby for the Host, to define the Playarea
 */
@Composable
fun GameRunningScreen(
    viewModel: RunningGameViewModel,
    mapLocationModel: MapLocationViewModel = viewModel(),
    currentPlayerId: String
) {
    val permissionGranted by mapLocationModel.permissionGranted.collectAsState()

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            mapLocationModel.onPermissionResult(granted)
        }
    //switch action based on permission state
    when (permissionGranted) {
        LocationPermissionState.Granted -> { //show game screen
            RunningGameScreenComponent(viewModel, mapLocationModel, currentPlayerId)
        }

        LocationPermissionState.RequestRequired -> { //ask for permission
            LaunchedEffect(Unit) {
                permissionLauncher.launch(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }

        LocationPermissionState.Denied -> { //permission denied
            PermissionDeniedScreen()
        }
    }
}

@Composable
fun RunningGameScreenComponent(
    viewModel: RunningGameViewModel,
    mapLocationModel: MapLocationViewModel,
    currentPlayerId: String
) {
    val lastLocation = mapLocationModel.currentLocation.collectAsState()
    LaunchedEffect(Unit) {
        mapLocationModel.loadCurrLocation()
    }

    when (lastLocation) {
        null -> CenteredLoadingIndicator(modifier = Modifier.padding(top = 100.dp))
        else -> {
            val playMapData = remember { PlayMapData() }
            GameMap(lastLocation.value!!, mapData = playMapData, viewModel)
        }
    }

    //TODO ADD MAP COMPOSABLE, SPAWN DEFAULT ON LAST LOCATION FROM mapLocationModel
    //TODO ADD POLYGON
    //TODO ADD BLIPS

}

@Composable
fun GameMap(startLocation: Location, mapData: PlayMapData, viewModel: RunningGameViewModel){
    Box(){
        mapData.CustomMap(
            modifier = Modifier.fillMaxSize(),
            lat = startLocation.latitude,
            lon = startLocation.longitude,
            appContext = LocalContext.current,
            onMapReady = { map ->
                val polygon = viewModel.gamestate.value!!.polygon
                for(poly in polygon){
                    map.addPolyPoint(LatLng(poly.latitude(), poly.longitude()))
                }
                //TODO Add poly to map.
            }
        )
    }
}