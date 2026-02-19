package de.leonseeger.scotlandyardinreallife.entity

import kotlinx.coroutines.flow.Flow

/**
 * Repository-Schnittstelle, die alle CRUD-Operationen für die [Player]-Entity innerhalb eines [Game] bereitstellt.
 *
 * Dokumentation erstellt mit KI (Perplexity – Claude Sonnet 4.6).
 *
 * @author Leon Seeger
 */
interface PlayerCatalog {
    suspend fun addPlayerToGame(gameId: String, player: Player): Result<String>
    suspend fun updatePlayer(gameId: String, player: Player): Result<Unit>
    suspend fun removePlayerFromGame(gameId: String, playerId: String): Result<Unit>

    fun getPlayer(gameId: String, playerId: String): Flow<Player?>
    fun getPlayersInGame(gameId: String): Flow<List<Player>?>
}
