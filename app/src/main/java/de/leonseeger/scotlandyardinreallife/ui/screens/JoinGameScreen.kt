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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.leonseeger.scotlandyardinreallife.ui.component.CustomTextField
import de.leonseeger.scotlandyardinreallife.ui.component.ErrorText
import de.leonseeger.scotlandyardinreallife.ui.component.PrimaryButton
import de.leonseeger.scotlandyardinreallife.ui.component.SectionTitle

@Composable
fun JoinGameScreen(
    onJoinWithCode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var gameCode by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SectionTitle(text = "Spiel beitreten")

        Spacer(modifier = Modifier.height(24.dp))

        CustomTextField(
            value = gameCode,
            onValueChange = {
                gameCode = it.trim()
                showError = false
            },
            label = "Spiel-Code",
            isError = showError,
            errorMessage = if (showError) "Bitte einen gültigen Code eingeben" else null
        )

        if (showError) {
            Spacer(modifier = Modifier.height(8.dp))
            ErrorText(text = "Der Spiel-Code muss mindestens 6 Zeichen lang sein")
        }

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = "Beitreten",
            onClick = {
                if (gameCode.length >= 6) {
                    onJoinWithCode(gameCode)
                } else {
                    showError = true
                }
            },
            icon = Icons.Default.PlayArrow,
            enabled = gameCode.isNotEmpty()
        )
    }
}