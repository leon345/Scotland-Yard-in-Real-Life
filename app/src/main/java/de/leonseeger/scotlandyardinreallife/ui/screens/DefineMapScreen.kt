package de.leonseeger.scotlandyardinreallife.ui.screens

import android.content.Context
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.leonseeger.scotlandyardinreallife.R
import de.leonseeger.scotlandyardinreallife.game.controll.DefinePlaymapViewModel
import de.leonseeger.scotlandyardinreallife.game.controll.LocationPermissionState
import de.leonseeger.scotlandyardinreallife.ui.component.CenteredLoadingIndicator
import de.leonseeger.scotlandyardinreallife.ui.component.gamemap.PlayMapData
import org.maplibre.geojson.Point
import kotlin.io.encoding.Base64

/**
 * The Map shown before the Game Lobby for the Host, to define the Playarea
 */
@Composable
fun DefineMapScreen(
    viewModel: DefinePlaymapViewModel = viewModel(),
    onMapDefined: (List<Point>) -> Unit
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
fun DefiningMap(viewModel: DefinePlaymapViewModel, onMapDefined: (List<Point>) -> Unit) {
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
    Box(modifier = Modifier.fillMaxSize()) {
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
        Text(stringResource(R.string.define_playarea),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 30.dp))
        Button(
            onClick = {
                val polygon = mapData.getPolygonPoints()
                if(polygon.size > 4)
                    onMapDefined(polygon)
                else
                    Toast.makeText(context, "Polygon braucht mehr Punkte", Toast.LENGTH_SHORT)
                        .show()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        ) {
            Text(stringResource(R.string.confirm))
        }
    }
}
