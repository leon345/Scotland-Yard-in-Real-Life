package de.leonseeger.scotlandyardinreallife.entity

import kotlinx.coroutines.flow.Flow

/**
 * Repository-Schnittstelle, die alle CRUD-Operationen für die [Game]-Entity bereitstellt.
 *
 * Dokumentation erstellt mit KI (Perplexity – Claude Sonnet 4.6).
 *
 * @author Leon Seeger
 */
interface GameCatalog {

    suspend fun createGame(game: Game): Result<String>
    fun getGameById(gameId: String): Flow<Game?>
    suspend fun updateGame(game: Game): Result<Unit>
    suspend fun deleteGame(gameId: String): Result<Unit>
    fun getGamesByStatus(status: GameStatus): Flow<List<Game>>
    suspend fun updateGameStatus(gameId: String, status: GameStatus): Result<Unit>
    suspend fun endGame(gameId: String, winner: PlayerRole): Result<Unit>
}