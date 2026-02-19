package de.leonseeger.scotlandyardinreallife.ui.models

import android.app.Application
import android.content.Context
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.leonseeger.scotlandyardinreallife.controll.LocationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapLocationViewModel(private val context: Application) : AndroidViewModel(context) {

    private val locationService = LocationProvider(context)


    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation = _currentLocation.asStateFlow()

    private val _permissionGranted = MutableStateFlow<LocationPermissionState>(
        LocationPermissionState.RequestRequired
    )
    val permissionGranted = _permissionGranted.asStateFlow()

    fun loadCurrLocation(){
        viewModelScope.launch {
            _currentLocation.value =
                locationService.getCurrentLocation()
        }
    }

    fun checkLocationPermission() {
        _permissionGranted.value =
            if (locationService.hasLocationPermissions()) {
                LocationPermissionState.Granted
            } else {
                LocationPermissionState.RequestRequired
            }
    }

    fun onPermissionResult(granted: Boolean) {
        _permissionGranted.value =
            if (granted) {
                LocationPermissionState.Granted
            } else {
                LocationPermissionState.Denied
            }
    }

    fun getContext(): Context {
        return context;
    }
}

sealed interface LocationPermissionState {
    object Granted : LocationPermissionState
    object Denied : LocationPermissionState
    object RequestRequired : LocationPermissionState
}