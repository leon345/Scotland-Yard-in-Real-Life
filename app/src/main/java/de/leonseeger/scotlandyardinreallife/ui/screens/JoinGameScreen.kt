package de.leonseeger.scotlandyardinreallife.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.leonseeger.scotlandyardinreallife.R
import de.leonseeger.scotlandyardinreallife.game.controll.CreateGameViewModel
import de.leonseeger.scotlandyardinreallife.navigation.NavigationRoutes
import de.leonseeger.scotlandyardinreallife.ui.component.CustomTextField
import de.leonseeger.scotlandyardinreallife.ui.component.ErrorText
import de.leonseeger.scotlandyardinreallife.ui.component.PrimaryButton
import de.leonseeger.scotlandyardinreallife.ui.component.SectionTitle

@Composable
fun JoinGameScreen(
    navController: NavController,
    viewModel: CreateGameViewModel,
    modifier: Modifier = Modifier
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val gameState by viewModel.gamestate.collectAsState()

    var gameCode by remember { mutableStateOf("") }

    LaunchedEffect(gameState) {
        gameState?.let { game ->
            if (error == null) {
                navController.navigate(
                    NavigationRoutes.gameLobby(mode = "JOIN", gameCode = game.id)
                ) {
                    popUpTo(NavigationRoutes.JOIN_GAME) { inclusive = true }
                }
            }
        }
    }




    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SectionTitle(text = stringResource(R.string.join_game_title))

        Spacer(modifier = Modifier.height(24.dp))

        CustomTextField(
            value = gameCode,
            onValueChange = {
                gameCode = it.trim()
                viewModel.clearError()
            },
            label = stringResource(R.string.game_code_label),
            isError = error != null,
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            ErrorText(text = stringResource(R.string.invalid_game_code_error))
        }

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = if (isLoading) stringResource(R.string.loading) else stringResource(R.string.join_button),
            onClick = {

                viewModel.joinGame(gameCode, "0")

            },
            icon = Icons.Default.PlayArrow,
            enabled = gameCode.length >= 6 && !isLoading
        )
    }
}