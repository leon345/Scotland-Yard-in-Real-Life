package de.leonseeger.scotlandyardinreallife.ui.component.gameloby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
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
import de.leonseeger.scotlandyardinreallife.ui.component.ErrorText
import de.leonseeger.scotlandyardinreallife.ui.component.PrimaryButton
import de.leonseeger.scotlandyardinreallife.ui.component.SectionTitle
import de.leonseeger.scotlandyardinreallife.ui.component.SubheadingText

@Composable
fun GameLobbyScreen(
    controller: CreateGameViewModel,
    gameId: String?,
    mode: String?,
    playerId: String,
    modifier: Modifier = Modifier,
    onStartGame: () -> Unit = {}
) {
    val gameState by controller.gamestate.collectAsState()
    val players by controller.players.collectAsState()
    val isLoading by controller.isLoading.collectAsState()
    val error by controller.error.collectAsState()

    LaunchedEffect(gameId) {
        when (mode) {
            "CREATE" -> controller.createGame(playerId)
            "JOIN" -> gameId?.let { controller.joinGame(it, playerId) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionTitle(
            text = stringResource(R.string.create_game_title)
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

                SubheadingText(
                    text = stringResource(R.string.participating_players, players.size)
                )

                PlayersList(
                    players = players, ownerId = game.owner.id, modifier = Modifier.weight(1f)
                )

                PrimaryButton(
                    text = stringResource(R.string.start_game), onClick = {
                        controller.startGame()
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