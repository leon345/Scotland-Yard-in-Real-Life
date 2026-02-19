package de.leonseeger.scotlandyardinreallife.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.leonseeger.scotlandyardinreallife.R

@Composable
fun PlayerOutOfBoundsNotification() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.grey_dark_transparent))
    ) {
        Column(
            modifier = Modifier
                .background(
                    colorResource(R.color.detective_color_bg),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(20.dp)
        ) {
            Text("Spiel pausiert", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.neon_yellow))
            Text("Spieler außerhalb Spielbereich", fontSize = 18.sp, color = colorResource(R.color.bandit_color))
        }

    }
}