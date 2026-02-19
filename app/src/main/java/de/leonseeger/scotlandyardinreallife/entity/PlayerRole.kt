package de.leonseeger.scotlandyardinreallife.entity

enum class PlayerRole {
    DETECTIVE, BANDIT;

    fun toggle(): PlayerRole = when (this) {
        DETECTIVE -> BANDIT
        BANDIT -> DETECTIVE
    }
}