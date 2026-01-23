package de.leonseeger.scotlandyardinreallife.game.entity

enum class PlayerRole {
    DETECTIVE, BANDIT;

    fun toggle(): PlayerRole = when (this) {
        DETECTIVE -> BANDIT
        BANDIT -> DETECTIVE
    }
}