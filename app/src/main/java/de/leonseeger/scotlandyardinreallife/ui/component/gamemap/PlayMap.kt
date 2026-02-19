package de.leonseeger.scotlandyardinreallife.ui.component.gamemap

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.leonseeger.scotlandyardinreallife.game.controll.LocationService
import org.maplibre.android.geometry.LatLng

@Composable
fun PlayMap(
    context: Context,
    mapData: PlayMapData,
    startLocation: LatLng,
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

