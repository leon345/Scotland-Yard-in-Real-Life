package de.leonseeger.scotlandyardinreallife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.leonseeger.scotlandyardinreallife.ui.components.CustomMap
import de.leonseeger.scotlandyardinreallife.ui.theme.ScotlandYardInRealLifeTheme
import org.maplibre.android.MapLibre

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val key = BuildConfig.MAPTILER_API_KEY
        val mapId = "landscape-v4"
        val styleUrl = "https://api.maptiler.com/maps/$mapId/style.json?key=$key"
        MapLibre.getInstance(this)

        enableEdgeToEdge()
        setContent {
            ScotlandYardInRealLifeTheme {
                CustomMap(modifier = Modifier.fillMaxSize(),
                    styleUrl = styleUrl
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ScotlandYardInRealLifeTheme {
        Greeting("Android")
    }
}