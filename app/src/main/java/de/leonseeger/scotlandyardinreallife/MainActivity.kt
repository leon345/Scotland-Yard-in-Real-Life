package de.leonseeger.scotlandyardinreallife

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.leonseeger.scotlandyardinreallife.game.boundary.CreateGameActivity
import de.leonseeger.scotlandyardinreallife.ui.component.PrimaryButton
import de.leonseeger.scotlandyardinreallife.ui.components.PlayMap
import de.leonseeger.scotlandyardinreallife.ui.theme.ScotlandYardInRealLifeTheme
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLng

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(this)
        enableEdgeToEdge()
        setContent {
            val playMap = remember { PlayMap() }




            ScotlandYardInRealLifeTheme {
                PlayScreen(
                    playMap = playMap,
                    context = LocalContext.current
                )
                /*CustomMap(modifier = Modifier.fillMaxSize(),
                    lat = 52.2720, lon = 8.0482,
                    appContext = this
                )*/
                /*Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }*/
            }
        }
    }
}

@Composable
fun PlayScreen(
    playMap: PlayMap,
    context: Context
) {
    playMap.CustomMap(
        modifier = Modifier.fillMaxSize(),
        lat = 52.2720,
        lon = 8.0482,
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
                if(!map.addPolyPoint(point)){
                    Toast.makeText(context, "Polygon braucht mehr Punkte", Toast.LENGTH_SHORT).show()
                }
                true
            }
        }
    )
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Scotland Yard in Real Life")

        PrimaryButton(
            text = "Spiel erstellen",
            onClick = {
                val intent = Intent(context, CreateGameActivity::class.java)
                context.startActivity(intent)
            },
            icon = Icons.Default.Add,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ScotlandYardInRealLifeTheme {
        MainScreen()
    }
}