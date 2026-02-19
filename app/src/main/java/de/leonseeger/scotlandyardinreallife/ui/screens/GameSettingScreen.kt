package de.leonseeger.scotlandyardinreallife.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.leonseeger.scotlandyardinreallife.R
import de.leonseeger.scotlandyardinreallife.ui.models.CreateGameViewModel
import de.leonseeger.scotlandyardinreallife.entity.GameSettings
import de.leonseeger.scotlandyardinreallife.ui.component.LabelText
import de.leonseeger.scotlandyardinreallife.ui.component.NumericTextField
import de.leonseeger.scotlandyardinreallife.ui.component.PrimaryButton
import de.leonseeger.scotlandyardinreallife.ui.component.SecondaryButton
import de.leonseeger.scotlandyardinreallife.ui.component.SectionTitle

@Composable
fun GameSettingScreen(
    viewModel: CreateGameViewModel, modifier: Modifier = Modifier, onNavigateBack: () -> Unit
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp, horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                SectionTitle(text = "Spieleinstellungen")
            }

            IconButton(
                onClick = {
                    gameDuration = GameSettings.DEFAULT.gameDuration.toString()
                    banditRevealInterval = GameSettings.DEFAULT.banditRevealInterval.toString()
                    gameDurationError = null
                    banditIntervalError = null
                }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Standardwerte wiederherstellen",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.detective_color_bg))
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(text = "Spieldauer", color = colorResource(R.color.neon_yellow), fontSize = 24.sp, fontWeight = FontWeight.Bold)

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
            }
        }


        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.detective_color_bg))
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(text = "Banditen-Offenbarungsintervall", color = colorResource(R.color.neon_yellow), fontSize = 24.sp, fontWeight = FontWeight.Bold)

                SettingInputField(
                    label = "Intervall (Minuten)",
                    value = banditRevealInterval,
                    onValueChange = {
                        banditRevealInterval = it
                        banditIntervalError = validateInterval(it)
                    },
                    errorMessage = banditIntervalError,
                    description = "Wie oft soll die Position des Banditen angezeigt werden? (1-30 Minuten)"
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SecondaryButton(
                text = stringResource(R.string.abort), onClick = onNavigateBack, modifier = Modifier.weight(1f)
            )

            PrimaryButton(
                text = stringResource(R.string.save),
                onClick = {
                    val duration = gameDuration.toLongOrNull()
                    val interval = banditRevealInterval.toLongOrNull()

                    if (duration != null && interval != null && gameDurationError == null && banditIntervalError == null) {
                        viewModel.updateGameSettings(duration, interval)
                        onNavigateBack()
                    }
                },
                enabled = gameDuration.toLongOrNull() != null && banditRevealInterval.toLongOrNull() != null && gameDurationError == null && banditIntervalError == null,
                modifier = Modifier.weight(1f)
            )
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
            text = description, color = MaterialTheme.colorScheme.onSurfaceVariant
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
