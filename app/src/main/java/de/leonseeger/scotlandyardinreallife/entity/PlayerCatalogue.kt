package de.leonseeger.scotlandyardinreallife.entity

import kotlinx.coroutines.flow.Flow

interface PlayerCatalogue {
    suspend fun addPlayerToGame(gameId: String, player: Player): Result<String>
    suspend fun updatePlayer(gameId: String, player: Player): Result<Unit>
    suspend fun removePlayerFromGame(gameId: String, playerId: String): Result<Unit>

    fun getPlayer(gameId: String, playerId: String): Flow<Player?>
    fun getPlayersInGame(gameId: String): Flow<List<Player>?>
}
