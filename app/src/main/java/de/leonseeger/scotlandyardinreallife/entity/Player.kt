package de.leonseeger.scotlandyardinreallife.entity

import android.location.Location

/**
 * Entity, die einen Spieler mit seiner aktuellen GPS-Position und zugewiesenen [PlayerRole] innerhalb eines [Game] repräsentiert.
 *
 * Dokumentation erstellt mit KI (Perplexity – Claude Sonnet 4.6).
 *
 * @author Leon Seeger
 */
data class Player(
    val id: String,
    val currentLocation: Location?,
    val role: PlayerRole
)
