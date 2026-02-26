package de.leonseeger.scotlandyardinreallife.entity

/**
 * Enum-Entity, die die Rolle eines [Player] im [Game] als DETECTIVE oder BANDIT definiert.
 *
 * Dokumentation erstellt mit KI (Perplexity – Claude Sonnet 4.6).
 *
 * @author LEon Seeger
 */
enum class PlayerRole {
    DETECTIVE, BANDIT;

    fun toggle(): PlayerRole = when (this) {
        DETECTIVE -> BANDIT
        BANDIT -> DETECTIVE
    }
}