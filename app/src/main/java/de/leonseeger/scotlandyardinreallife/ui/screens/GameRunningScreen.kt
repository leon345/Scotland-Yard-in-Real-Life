package de.leonseeger.scotlandyardinreallife.ui.screens

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import de.leonseeger.scotlandyardinreallife.R
import de.leonseeger.scotlandyardinreallife.game.controll.CreateGameViewModel
import de.leonseeger.scotlandyardinreallife.game.controll.LocationPermissionState
import de.leonseeger.scotlandyardinreallife.game.controll.MapLocationViewModel
import de.leonseeger.scotlandyardinreallife.game.entity.GameStatus
import de.leonseeger.scotlandyardinreallife.game.entity.PlayerRole
import de.leonseeger.scotlandyardinreallife.ui.component.CenteredLoadingIndicator
import de.leonseeger.scotlandyardinreallife.ui.component.gamemap.PlayMapData
import org.maplibre.android.geometry.LatLng

/**
 * The Map shown before the Game Lobby for the Host, to define the Playarea
 */
@Composable
fun GameRunningScreen(
    viewModel: CreateGameViewModel,
    mapLocationModel: MapLocationViewModel = viewModel(),
    onGameEnd: () -> Unit = {}
) {
    val permissionGranted by mapLocationModel.permissionGranted.collectAsState()
    val game by viewModel.gamestate.collectAsState()
    val currPlayerId by viewModel.currentPlayerId.collectAsState()
    val serviceContext = LocalContext.current

    LaunchedEffect(game?.status) {
        if (game?.status == GameStatus.FINISHED) {
            onGameEnd()
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            mapLocationModel.onPermissionResult(granted)
        }
    //switch action based on permission state
    when (permissionGranted) {
        LocationPermissionState.Granted -> { //show game screen
            /*Launching location service */
            val gameId = game?.id
            val playerId = currPlayerId
            if (gameId != null && playerId != null)
                LaunchedEffect(Unit) {
                    viewModel.startLocationServices(serviceContext)
                    Log.d("Location Service", "Started Location Service")
                }
            /*-------*/
            RunningGameScreenComponent(viewModel, mapLocationModel, serviceContext)
        }

        LocationPermissionState.RequestRequired -> { //ask for permission
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
fun RunningGameScreenComponent(
    viewModel: CreateGameViewModel,
    mapLocationModel: MapLocationViewModel,
    serviceContext: Context
) {
    val lastLocation by mapLocationModel.currentLocation.collectAsState()
    LaunchedEffect(Unit) {
        mapLocationModel.loadCurrLocation()
    }
    val gamestate by viewModel.gamestate.collectAsState()

    if (gamestate == null || lastLocation == null) {
        CenteredLoadingIndicator(modifier = Modifier.padding(top = 100.dp))
    } else {
        val playMapData = remember { PlayMapData() }
        GameMap(lastLocation!!, mapData = playMapData, viewModel, serviceContext)
    }
}

@Composable
fun GameMap(startLocation: Location, mapData: PlayMapData, viewModel: CreateGameViewModel, serviceContext: Context) {
    var openEndDialog = remember { mutableStateOf(false) }
    var openOutOfBoundsAlert = remember { mutableStateOf(false) }

    val players by viewModel.players.collectAsState()
    var mapReady by remember { mutableStateOf(false) }
    val playerRole: PlayerRole = viewModel.getCurrPlayerRole()
    val playerColor = if (playerRole == PlayerRole.BANDIT) colorResource(R.color.bandit_color)
    else colorResource(R.color.detective_color)
    val playerBgColor = if (playerRole == PlayerRole.BANDIT) colorResource(R.color.bandit_color_bg)
    else colorResource(R.color.detective_color_bg)

    Box() {
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
                if (polygon != null) {
                    Log.w("Map", "Poly not null")
                    for (poly in polygon) {
                        Log.w("Map", "Adding Poly to map")
                        map.addPolyPoint(LatLng(poly.latitude(), poly.longitude()), true)
                    }
                }
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            /*---Start Role Box---*/
            Box(
                modifier = Modifier
                    .border(
                        2.dp,
                        colorResource(R.color.dark),
                        RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier
                        .blur(12.dp)
                        .background(
                            playerBgColor,
                            RoundedCornerShape(8.dp),
                        )
                        .matchParentSize()
                )
                Text(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = playerColor,
                    text = playerRole.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            /*---End Role Box---*/
            /*---Start Controls---*/
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            ) {
                if (playerRole == PlayerRole.BANDIT) {
                    Button(
                        colors = ButtonColors(
                            colorResource(R.color.detective_color_dark),
                            contentColor = colorResource(R.color.neon_yellow),
                            disabledContainerColor = colorResource(R.color.grey),
                            disabledContentColor = colorResource(R.color.black)
                        ),
                        onClick = {
                            //TODO Execute found actions
                            openEndDialog.value = true
                        },
                    ) {
                        Text(stringResource(R.string.found), fontSize = 18.sp)
                    }
                }
            }

        }
    }

    if (mapReady) {
        LaunchedEffect(players) {
            var playerOutOfBounds = false
            for (player in players) {
                if(!player.inBounds)
                    playerOutOfBounds = true
            }
            if(playerOutOfBounds) {
                openOutOfBoundsAlert.value = true
                Log.w("Player Location", "Player out of bounds")
            } else{ //only update player positions if all players in bounds
                mapData.updatePlayers(players)
                openOutOfBoundsAlert.value = false
            }
        }
    }

    when {
        openEndDialog.value -> {
            EndGameDialog(
                onDismissRequest = {
                    openEndDialog.value = false
                    viewModel.stopLocationServices(serviceContext)
                },
                onConfirmation = {
                    openEndDialog.value = false
                    viewModel.endGame()
                },
                title = "Spiel beenden?",
                content = stringResource(R.string.foud_question)
            )
        }
        openOutOfBoundsAlert.value -> {
            PlayerOutOfBoundsDialog(onDismissRequest = {})
        }
    }
}

@Composable
fun EndGameDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    title: String,
    content: String
) {
    AlertDialog(
        title = { Text(text = title) },
        text = { Text(text = content) },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                colors = ButtonColors(
                    colorResource(R.color.detective_color_dark),
                    contentColor = colorResource(R.color.neon_yellow),
                    disabledContainerColor = colorResource(R.color.grey),
                    disabledContentColor = colorResource(R.color.black)
                ),
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(stringResource(R.string.confirm_found))
            }
        },
        dismissButton = {
            TextButton(
                colors = ButtonColors(
                    colorResource(R.color.neon_yellow),
                    contentColor = colorResource(R.color.detective_color_dark),
                    disabledContainerColor = colorResource(R.color.grey),
                    disabledContentColor = colorResource(R.color.black)
                ),
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.abort))
            }
        }
    )
}

@Composable
fun PlayerOutOfBoundsDialog(
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = {
        onDismissRequest()
    }) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Spieler außerhalb des Spielbereiches!", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = colorResource(R.color.red_mid))
        }
    }
}
