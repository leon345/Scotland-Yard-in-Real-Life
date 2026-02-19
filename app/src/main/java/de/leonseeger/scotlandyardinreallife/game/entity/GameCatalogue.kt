package de.leonseeger.scotlandyardinreallife.game.entity

import kotlinx.coroutines.flow.Flow

interface GameCatalogue {

    suspend fun createGame(game: Game): Result<String>
    fun getGameById(gameId: String): Flow<Game?>
    suspend fun updateGame(game: Game): Result<Unit>
    suspend fun deleteGame(gameId: String): Result<Unit>
    fun getGamesByStatus(status: GameStatus): Flow<List<Game>>
    suspend fun updateGameStatus(gameId: String, status: GameStatus): Result<Unit>
    suspend fun endGame(gameId: String, winner: PlayerRole): Result<Unit>
}