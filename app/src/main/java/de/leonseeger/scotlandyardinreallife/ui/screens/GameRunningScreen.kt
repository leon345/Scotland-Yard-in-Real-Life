package de.leonseeger.scotlandyardinreallife.ui.screens

import RunningGameViewModel
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.util.Log
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.leonseeger.scotlandyardinreallife.game.controll.CreateGameViewModel
import de.leonseeger.scotlandyardinreallife.game.controll.LocationPermissionState
import de.leonseeger.scotlandyardinreallife.game.controll.LocationService
import de.leonseeger.scotlandyardinreallife.game.controll.MapLocationViewModel
import de.leonseeger.scotlandyardinreallife.ui.component.CenteredLoadingIndicator
import de.leonseeger.scotlandyardinreallife.ui.component.gamemap.PlayMapData
import org.maplibre.android.geometry.LatLng

/**
 * The Map shown before the Game Lobby for the Host, to define the Playarea
 */
@Composable
fun GameRunningScreen(
    viewModel: CreateGameViewModel,
    mapLocationModel: MapLocationViewModel = viewModel()
) {
    val permissionGranted by mapLocationModel.permissionGranted.collectAsState()
    val game = viewModel.gamestate.collectAsState()
    val currPlayerId = viewModel.currentPlayerId.collectAsState()
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            mapLocationModel.onPermissionResult(granted)
        }
    //switch action based on permission state
    when (permissionGranted) {
        LocationPermissionState.Granted -> { //show game screen
            val gameId = game.value?.id
            val playerId = currPlayerId.value
            if(gameId != null && playerId != null)
                startLocationService(LocalContext.current, gameId, playerId)
            RunningGameScreenComponent(viewModel, mapLocationModel)
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
    viewModel: CreateGameViewModel,
    mapLocationModel: MapLocationViewModel
) {
    val lastLocation = mapLocationModel.currentLocation.collectAsState()
    LaunchedEffect(Unit) {
        mapLocationModel.loadCurrLocation()
    }

    val gamestate by viewModel.gamestate.collectAsState()  // StateFlow oder LiveData

    if (gamestate == null || lastLocation.value == null) {
        CenteredLoadingIndicator(modifier = Modifier.padding(top = 100.dp))
    } else {
        val playMapData = remember { PlayMapData() }
        GameMap(lastLocation.value!!, mapData = playMapData, viewModel)
    }
}

@Composable
fun GameMap(startLocation: Location, mapData: PlayMapData, viewModel: CreateGameViewModel){
    val players by viewModel.players.collectAsState()
    var mapReady by remember { mutableStateOf(false) }

    Box(){
        mapData.CustomMap(
            modifier = Modifier.fillMaxSize(),
            lat = startLocation.latitude,
            lon = startLocation.longitude,
            invert = true,
            appContext = LocalContext.current,
            onMapReady = { map ->
                Log.w("Map", "Map ready")
                mapReady = true
                val polygon = viewModel.gamestate.value?.polygon
                if(polygon != null){
                    Log.w("Map", "Poly not null")
                    for(poly in polygon){
                        Log.w("Map", "Adding Poly to map")
                        map.addPolyPoint(LatLng(poly.latitude(), poly.longitude()), true)
                    }
                }
            }
        )
    }

    if (mapReady) {
        LaunchedEffect(players) {
            mapData.updatePlayers(players)
        }
    }
}