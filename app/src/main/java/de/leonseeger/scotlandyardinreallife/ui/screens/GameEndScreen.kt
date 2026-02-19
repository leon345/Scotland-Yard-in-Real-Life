package de.leonseeger.scotlandyardinreallife.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.leonseeger.scotlandyardinreallife.R

@Composable
fun GameEndScreen(
    winMsg: String,
    onBackHome: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .background(colorResource(R.color.detective_color_bg), shape = RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(R.string.game_over_message),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.neon_yellow)
            )
            Text(winMsg)
            TextButton(
                colors = ButtonColors(
                    colorResource(R.color.neon_yellow),
                    contentColor = colorResource(R.color.detective_color_dark),
                    disabledContainerColor = colorResource(R.color.grey),
                    disabledContentColor = colorResource(R.color.black)
                ),
                onClick = {
                    onBackHome()
                }
            ) {
                Text(stringResource(R.string.back_home))
            }
        }
    }
}