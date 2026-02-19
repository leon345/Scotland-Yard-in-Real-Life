package de.leonseeger.scotlandyardinreallife.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.leonseeger.scotlandyardinreallife.R
import de.leonseeger.scotlandyardinreallife.ui.component.PrimaryButton
import de.leonseeger.scotlandyardinreallife.ui.component.SecondaryButton
import de.leonseeger.scotlandyardinreallife.ui.component.SectionTitle
import de.leonseeger.scotlandyardinreallife.ui.component.SubheadingText


@Composable
fun HomeScreen(
    onCreateGame: () -> Unit,
    onJoinGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionTitle(
            text = "Scotland Yard"
        )


        SubheadingText(
            text = "In Real Life",
            modifier = Modifier.padding(bottom = 48.dp)
        )


        PrimaryButton(
            text = stringResource(R.string.create_game),
            onClick = onCreateGame,
            icon = Icons.Default.Add
        )

        Spacer(modifier = Modifier.height(16.dp))


        SecondaryButton(
            text = stringResource(R.string.join_game),
            onClick = onJoinGame,
            icon = Icons.Default.PlayArrow
        )
    }
}