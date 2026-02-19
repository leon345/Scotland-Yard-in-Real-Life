package de.leonseeger.scotlandyardinreallife.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.leonseeger.scotlandyardinreallife.R
import de.leonseeger.scotlandyardinreallife.game.controll.CreateGameViewModel
import de.leonseeger.scotlandyardinreallife.game.entity.GameStatus
import de.leonseeger.scotlandyardinreallife.game.entity.Game
import de.leonseeger.scotlandyardinreallife.ui.component.ErrorText
import de.leonseeger.scotlandyardinreallife.ui.component.PrimaryButton
import de.leonseeger.scotlandyardinreallife.ui.component.SectionTitle
import de.leonseeger.scotlandyardinreallife.ui.component.SubheadingText
import de.leonseeger.scotlandyardinreallife.ui.component.gameloby.InvitationCodeCard
import de.leonseeger.scotlandyardinreallife.ui.component.gameloby.PlayersList
import org.maplibre.geojson.Point

@Composable
fun GameLobbyScreen(
    viewModel: CreateGameViewModel,
    gameId: String?,
    mode: String?,
    playerId: String,
    modifier: Modifier = Modifier,
    playArea: List<Point>?,
    onStartGame: () -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    val gameState by viewModel.gamestate.collectAsState()
    val players by viewModel.players.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        if (gameState == null) {
            when (mode) {
                "CREATE" -> viewModel.createGame(playerId)
                "JOIN" -> gameId?.let { viewModel.joinGame(it, playerId) }
            }
        }
    }

    LaunchedEffect(gameState?.status) {
        if (gameState?.status == GameStatus.RUNNING) {
            onStartGame();
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle(
                text = stringResource(R.string.create_game_title)
            )

            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Spieleinstellungen",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
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

                SubheadingText(
                    text = stringResource(R.string.participating_players, players.size)
                )

                PlayersList(
                    players = players,
                    ownerId = game.owner.id,
                    modifier = Modifier.weight(1f),
                    onPlayerClick = { clickedPlayerId ->
                        viewModel.togglePlayerRole(clickedPlayerId)
                    }
                )

                PrimaryButton(
                    text = stringResource(R.string.start_game), onClick = {
                        viewModel.startGame()
                        onStartGame()
                    }, enabled = players.size >= 2, icon = Icons.Default.PlayArrow
                )

                if (players.size < 2) {
                    ErrorText(
                        text = stringResource(R.string.minimum_players_required)
                    )
                }

            }
        }
    }

}