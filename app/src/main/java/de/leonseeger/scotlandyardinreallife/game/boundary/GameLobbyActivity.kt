package de.leonseeger.scotlandyardinreallife.game.boundary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.google.firebase.firestore.FirebaseFirestore
import de.leonseeger.scotlandyardinreallife.game.CreateGameViewModelFactory
import de.leonseeger.scotlandyardinreallife.game.controll.CreateGameViewModel
import de.leonseeger.scotlandyardinreallife.game.gateway.FirebaseGateway
import de.leonseeger.scotlandyardinreallife.ui.component.gameloby.GameLobbyScreen
import de.leonseeger.scotlandyardinreallife.ui.theme.ScotlandYardInRealLifeTheme

class CreateGameActivity : ComponentActivity() {
    private val firebaseGateway = FirebaseGateway(FirebaseFirestore.getInstance())
    private val controller: CreateGameViewModel by viewModels {
        CreateGameViewModelFactory(
            gameCatalogue = firebaseGateway,
            playerCatalogue = firebaseGateway
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val mode = intent.getStringExtra("MODE")
        val gameId = intent.getStringExtra("GAME_ID") //TODO
        val ownerId = "user_${System.currentTimeMillis()}" //TODO

        setContent {
            ScotlandYardInRealLifeTheme() {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameLobbyScreen(
                        controller = controller,
                        gameId = gameId,
                        mode = mode,
                        playerId = ownerId,
                        modifier = Modifier.padding(innerPadding),
                        onStartGame = {
                            // TODO: Navigation zum Spiel-Screen
                            finish()
                        }
                    )
                }
            }
        }
    }


}







