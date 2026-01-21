package de.leonseeger.scotlandyardinreallife

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import de.leonseeger.scotlandyardinreallife.ui.components.CustomMap
import androidx.compose.ui.unit.dp
import de.leonseeger.scotlandyardinreallife.game.boundary.CreateGameActivity
import de.leonseeger.scotlandyardinreallife.ui.component.PrimaryButton
import de.leonseeger.scotlandyardinreallife.ui.theme.ScotlandYardInRealLifeTheme
import org.maplibre.android.MapLibre

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mapId = "landscape-v4"
        val styleUrl = "https://tiles.openfreemap.org/styles/liberty"
        MapLibre.getInstance(this)

        enableEdgeToEdge()
        setContent {
            ScotlandYardInRealLifeTheme {
                CustomMap(modifier = Modifier.fillMaxSize(),
                    styleUrl = styleUrl, lat = 52.2720, lon = 8.0482,
                    appContext = this
                )
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