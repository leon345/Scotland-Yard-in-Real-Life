package de.leonseeger.scotlandyardinreallife.game.gateway

import android.location.Location
import com.google.firebase.firestore.FirebaseFirestore
import de.leonseeger.scotlandyardinreallife.game.entity.Game
import de.leonseeger.scotlandyardinreallife.game.entity.GameCatalogue
import de.leonseeger.scotlandyardinreallife.game.entity.GameStatus
import de.leonseeger.scotlandyardinreallife.game.entity.Player
import de.leonseeger.scotlandyardinreallife.game.entity.PlayerCatalogue
import kotlinx.coroutines.flow.Flow

class FirebaseGateway(private val firestore: FirebaseFirestore) : PlayerCatalogue, GameCatalogue {
    override suspend fun addPlayerToGame(
        gameId: String,
        playerId: String
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun updatePlayer(
        gameId: String,
        player: Player
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun removePlayerFromGame(
        gameId: String,
        playerId: String
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun getPlayer(
        gameId: String,
        playerId: String
    ): Flow<Player?> {
        TODO("Not yet implemented")
    }

    override fun getPlayersInGame(gameId: String): Flow<List<Player?>> {
        TODO("Not yet implemented")
    }

    override fun observePlayerLocations(gameId: String): Flow<Map<Player, String>> {
        TODO("Not yet implemented")
    }

    override suspend fun updatePlayerLocation(
        gameId: String,
        playerId: String,
        location: Location
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun createGame(game: Game): Result<String> {
        TODO("Not yet implemented")
    }

    override fun getGameById(gameId: String): Flow<Game?> {
        TODO("Not yet implemented")
    }

    override suspend fun updateGame(game: Game): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGame(gameId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override fun getGamesByStatus(status: GameStatus): Flow<List<Game>> {
        TODO("Not yet implemented")
    }

    override fun observeActiveGame(): Flow<Game> {
        TODO("Not yet implemented")
    }

    override suspend fun updateGameStatus(
        gameId: String,
        status: GameStatus
    ): Result<Unit> {
        TODO("Not yet implemented")
    }
}