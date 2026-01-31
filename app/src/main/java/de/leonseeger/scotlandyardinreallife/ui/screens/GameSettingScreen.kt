package de.leonseeger.scotlandyardinreallife.ui.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.leonseeger.scotlandyardinreallife.game.controll.CreateGameViewModel
import de.leonseeger.scotlandyardinreallife.game.entity.GameSettings
import de.leonseeger.scotlandyardinreallife.ui.component.LabelText
import de.leonseeger.scotlandyardinreallife.ui.component.NumericTextField
import de.leonseeger.scotlandyardinreallife.ui.component.PrimaryButton
import de.leonseeger.scotlandyardinreallife.ui.component.SecondaryButton
import de.leonseeger.scotlandyardinreallife.ui.component.SectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSettingScreen(
    viewModel: CreateGameViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit
) {
    val currentSetting by viewModel.gameSettings.collectAsState()

    var gameDuration by remember(currentSetting) {
        mutableStateOf(currentSetting.gameDuration.toString())
    }
    var banditRevealInterval by remember(currentSetting) {
        mutableStateOf(currentSetting.banditRevealInterval.toString())
    }

    var gameDurationError by remember { mutableStateOf<String?>(null) }
    var banditIntervalError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { SectionTitle(text = "Spieleinstellungen") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            gameDuration = GameSettings.DEFAULT.gameDuration.toString()
                            banditRevealInterval =
                                GameSettings.DEFAULT.banditRevealInterval.toString()
                            gameDurationError = null
                            banditIntervalError = null
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Standardwerte wiederherstellen"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            LabelText(
                text = "Passen Sie die Spieleinstellungen nach ihren Bedürfnissen an",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingInputField(
                label = "Spieldauer (Minuten)",
                value = gameDuration,
                onValueChange = {
                    gameDuration = it
                    gameDurationError = validateDuration(it)
                },
                errorMessage = gameDurationError,
                description = "Wie lange soll das Spiel dauern? (15-180 Minuten)"
            )

            SettingInputField(
                label = "Banditen-Offenbarungsintervall (Minuten)",
                value = banditRevealInterval,
                onValueChange = {
                    banditRevealInterval = it
                    banditIntervalError = validateInterval(it)
                },
                errorMessage = banditIntervalError,
                description = "Wie oft soll die Position des Banditen angezeigt werden? (1-30 Minuten)"
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SecondaryButton(
                    text = "Abbrechen",
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                )

                PrimaryButton(
                    text = "Speichern",
                    onClick = {
                        val duration = gameDuration.toLongOrNull()
                        val interval = banditRevealInterval.toLongOrNull()

                        if (duration != null && interval != null &&
                            gameDurationError == null && banditIntervalError == null
                        ) {
                            viewModel.updateGameSettings(duration, interval)
                            onNavigateBack()
                        }
                    },
                    enabled = gameDuration.toLongOrNull() != null &&
                            banditRevealInterval.toLongOrNull() != null &&
                            gameDurationError == null &&
                            banditIntervalError == null,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


@Composable
private fun SettingInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String?,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        NumericTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            isError = errorMessage != null,
            errorMessage = errorMessage
        )

        Spacer(modifier = Modifier.height(4.dp))

        LabelText(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


private fun validateDuration(value: String): String? {
    val number = value.toLongOrNull()
    return when {
        number == null -> "Bitte eine Zahl eingeben"
        number < 15 -> "Mindestens 15 Minuten erforderlich"
        number > 180 -> "Maximal 180 Minuten erlaubt"
        else -> null
    }
}

private fun validateInterval(value: String): String? {
    val number = value.toLongOrNull()
    return when {
        number == null -> "Bitte eine Zahl eingeben"
        number < 1 -> "Mindestens 1 Minute erforderlich"
        number > 30 -> "Maximal 30 Minuten erlaubt"
        else -> null
    }
}