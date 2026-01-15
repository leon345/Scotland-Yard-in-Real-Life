package de.leonseeger.scotlandyardinreallife.game.boundary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import de.leonseeger.scotlandyardinreallife.game.controll.CreateGameController
import de.leonseeger.scotlandyardinreallife.game.entity.Player
import de.leonseeger.scotlandyardinreallife.game.gateway.FirebaseGateway
import de.leonseeger.scotlandyardinreallife.ui.component.PrimaryButton
import de.leonseeger.scotlandyardinreallife.ui.theme.ScotlandYardInRealLifeTheme

class CreateGameActivity : ComponentActivity() {
    private val firebaseGateway = FirebaseGateway(FirebaseFirestore.getInstance())
    private lateinit var controller: CreateGameController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        controller = CreateGameController(
            gameCatalogue = firebaseGateway,
            playerCatalogue = firebaseGateway
        )

        val gameId = null //TODO
        val ownerId = "user_${System.currentTimeMillis()}" //TODO
        setContent {
            ScotlandYardInRealLifeTheme() {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CreateGameScreen(
                        controller = controller,
                        gameId = gameId,
                        ownerId = ownerId,
                        modifier = Modifier.padding(innerPadding),
                        onStartGame = {
                            // TODO: Navigation zum Spiel-Screen
                        }
                    )
                }
            }
        }
    }


}

@Composable
fun CreateGameScreen(
    controller: CreateGameController,
    gameId: String?,
    ownerId: String,
    modifier: Modifier = Modifier,
    onStartGame: () -> Unit = {}
) {
    val gameState by controller.gamestate.collectAsState()
    val players by controller.players.collectAsState()
    val isLoading by controller.isLoading.collectAsState()
    val error by controller.error.collectAsState()

    LaunchedEffect(gameId) {
        if (gameId != null) {
            controller.observeGame(gameId)
        } else {
            controller.createGame(ownerId);
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Spiel erstellen",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            gameState?.let { game ->
                InvitationCodeCard(gameId = game.id)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Teilnehemende Spieler (${players.size})",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )

                PlayersList(
                    players = players, ownerId = game.owner.id, modifier = Modifier.weight(1f)
                )

                PrimaryButton(
                    text = "Spiel starten", onClick = {
                        controller.startGame()
                        onStartGame()
                    }, enabled = players.size >= 2, icon = Icons.Default.PlayArrow
                )

                if (players.size < 2) {
                    Text(
                        text = "Mindestens 2 Spieler erforderlich",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

            }
        }
    }

}

@Composable
fun InvitationCodeCard(gameId: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Einladungscode",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = gameId.take(8).uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Teile diesen Code mit anderen Spielern",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PlayersList(
    players: List<Player>, ownerId: String, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (players.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Warte auf Spieler...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = players, key = { it.id }

                ) { players ->
                    PlayerItem(
                        player = players, isOwner = players.id == ownerId

                    )

                }
            }
        }
    }
}

@Composable
fun PlayerItem(player: Player, isOwner: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = if (isOwner) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ), elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Spieler ${player.id.take(6)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = player.role.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (isOwner) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Host",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}