package de.leonseeger.scotlandyardinreallife.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.leonseeger.scotlandyardinreallife.game.controll.DefinePlaymapViewModel
import de.leonseeger.scotlandyardinreallife.game.controll.LocationPermissionState

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
        null -> Text("No location available", Modifier.padding(30.dp))
        else -> Text(
            "Lat: ${location!!.latitude}, Lng: ${location!!.longitude}",
            Modifier.padding(30.dp)
        )
    }

    Button(onClick = { viewModel.loadCurrLocation() },
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 60.dp)) {
        Text("Reload")
    }
}