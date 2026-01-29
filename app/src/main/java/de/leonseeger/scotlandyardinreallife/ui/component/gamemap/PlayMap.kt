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
    playMap: PlayMap,
    startLocation: LatLng
) {
    val currentLocation = LocationService
    playMap.CustomMap(
        modifier = Modifier.fillMaxSize(),
        lat = startLocation.latitude,
        lon = startLocation.longitude,
        appContext = context,
        onMapReady = { map ->
            /*var positions = arrayOf(
                LatLng(52.267, 8.0532),
                LatLng(52.272, 8.0575),
                LatLng(52.281, 8.0432),
                LatLng(52.273, 8.0402)
            )*/
            //map.addPolyToMap(positions);
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

