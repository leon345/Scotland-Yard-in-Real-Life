package de.leonseeger.scotlandyardinreallife.game.entity

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface PlayerCatalogue {
    suspend fun addPlayerToGame(gameId: String, player: Player): Result<Unit>
    suspend fun updatePlayer(gameId: String, player: Player): Result<Unit>
    suspend fun removePlayerFromGame(gameId: String, playerId: String): Result<Unit>

    fun getPlayer(gameId: String, playerId: String): Flow<Player?>
    fun getPlayersInGame(gameId: String): Flow<List<Player>?>
    fun observePlayerLocations(gameId: String): Flow<Map<Player, String>>


    //TODO vielleicht nicht nötig
    suspend fun updatePlayerLocation(
        gameId: String,
        playerId: String,
        location: Location
    ): Result<Unit>
}
