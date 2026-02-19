package de.leonseeger.scotlandyardinreallife.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import de.leonseeger.scotlandyardinreallife.R

/**
 * Composable, das einen konfigurierbaren Bestätigungs-Dialog zum Beenden eines [Game] rendert.
 *
 * Dokumentation erstellt mit KI (Perplexity – Claude Sonnet 4.6).
 *
 * @author TODO Author
 */
@Composable
fun EndGameDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    title: String,
    content: String
) {
    AlertDialog(
        title = { Text(text = title) },
        text = { Text(text = content) },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                colors = ButtonColors(
                    colorResource(R.color.detective_color_dark),
                    contentColor = colorResource(R.color.neon_yellow),
                    disabledContainerColor = colorResource(R.color.grey),
                    disabledContentColor = colorResource(R.color.black)
                ),
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(stringResource(R.string.confirm_found))
            }
        },
        dismissButton = {
            TextButton(
                colors = ButtonColors(
                    colorResource(R.color.neon_yellow),
                    contentColor = colorResource(R.color.detective_color_dark),
                    disabledContainerColor = colorResource(R.color.grey),
                    disabledContentColor = colorResource(R.color.black)
                ),
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.abort))
            }
        }
    )
}