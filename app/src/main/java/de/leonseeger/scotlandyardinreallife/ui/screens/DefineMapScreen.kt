package de.leonseeger.scotlandyardinreallife.ui.screens

import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.leonseeger.scotlandyardinreallife.R
import de.leonseeger.scotlandyardinreallife.game.controll.MapLocationViewModel
import de.leonseeger.scotlandyardinreallife.game.controll.LocationPermissionState
import de.leonseeger.scotlandyardinreallife.ui.component.CenteredLoadingIndicator
import de.leonseeger.scotlandyardinreallife.ui.component.gamemap.PlayMapData
import org.maplibre.geojson.Point

/**
 * The Map shown before the Game Lobby for the Host, to define the Playarea
 */
@Composable
fun DefineMapScreen(
    viewModel: MapLocationViewModel = viewModel(),
    onMapDefined: (List<Point>) -> Unit
) {
    val permissionGranted by viewModel.permissionGranted.collectAsState()

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            viewModel.onPermissionResult(granted)
        }
    //switch action based on permission state
    when (permissionGranted) {
        LocationPermissionState.Granted -> {
            DefiningMap(viewModel, onMapDefined)
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
fun DefiningMap(viewModel: MapLocationViewModel, onMapDefined: (List<Point>) -> Unit) {
    val location by viewModel.currentLocation.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCurrLocation()
    }
    when (location) {
        null -> CenteredLoadingIndicator(modifier = Modifier.padding(top = 100.dp))
        else -> {
            val playMapData = remember { PlayMapData() }
            PlayareaDefinitionMap(viewModel.getContext(), playMapData, location!!, onMapDefined)
        }
    }
}

@Composable
fun PlayareaDefinitionMap(
    context: Context,
    mapData: PlayMapData,
    startLocation: Location,
    onMapDefined: (List<Point>) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
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
                    map.addMarker(point);
                    true
                }
            }
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .border(
                        2.dp,
                        colorResource(R.color.neon_yellow),
                        RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier
                        .blur(12.dp)
                        .background(
                            colorResource(R.color.blue_transparent),
                            RoundedCornerShape(8.dp)
                        )
                        .matchParentSize()
                )
                Text(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = colorResource(R.color.neon_yellow),
                    text = stringResource(R.string.define_playarea),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        ) {
            Button(
                colors = ButtonColors(colorResource(R.color.neon_yellow), contentColor = colorResource(R.color.black),
                    disabledContainerColor = colorResource(R.color.grey), disabledContentColor = colorResource(R.color.black)),
                onClick = {
                    mapData.removeLastPolyPoint()
                },
                ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo"
                )
            }
            Button(
                colors = ButtonColors(colorResource(R.color.detective_color_dark), contentColor = colorResource(R.color.neon_yellow),
                    disabledContainerColor = colorResource(R.color.grey), disabledContentColor = colorResource(R.color.black)),
                onClick = {
                    val polygon = mapData.getPolygonPoints()
                    if (polygon.size > 3)
                        onMapDefined(polygon)
                    else
                        Toast.makeText(context, "Polygon braucht mehr Punkte", Toast.LENGTH_SHORT)
                            .show()
                },

                ) {
                Text(stringResource(R.string.confirm))
            }
        }

    }
}
