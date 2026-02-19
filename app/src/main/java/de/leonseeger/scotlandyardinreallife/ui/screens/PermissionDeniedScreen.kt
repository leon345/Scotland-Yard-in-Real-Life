package de.leonseeger.scotlandyardinreallife.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


/**
 * Used for showing an Error, when the location permission is denied,
 * to show explanation and redirect back to home
 */
@Composable
fun PermissionDeniedScreen (){
    Text("Location Permission denied")
}