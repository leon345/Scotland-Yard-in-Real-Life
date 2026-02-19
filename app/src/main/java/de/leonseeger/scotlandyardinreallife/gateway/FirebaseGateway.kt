package de.leonseeger.scotlandyardinreallife.gateway

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.leonseeger.scotlandyardinreallife.entity.Game
import de.leonseeger.scotlandyardinreallife.entity.GameCatalog
import de.leonseeger.scotlandyardinreallife.entity.GameStatus
import de.leonseeger.scotlandyardinreallife.entity.Player
import de.leonseeger.scotlandyardinreallife.entity.PlayerCatalog
import de.leonseeger.scotlandyardinreallife.entity.PlayerRole
import de.leonseeger.scotlandyardinreallife.gateway.dto.GameDto
import de.leonseeger.scotlandyardinreallife.gateway.dto.toDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

/**
 * Firebase-Implementierung der [PlayerCatalog]- und [GameCatalog]-Schnittstellen,
 * die alle CRUD-Operationen für [Game]- und [Player]-Entities über Firestore realisiert.
 *
 * Dokumentation erstellt mit KI (Perplexity – Claude Sonnet 4.6).
 *
 * @author Leon Seeger & Jannes Schophuis
 */
class FirebaseGateway(private val firestore: FirebaseFirestore) : PlayerCatalog, GameCatalog {
    private val gamesCollection = firestore.collection("games")

    override suspend fun addPlayerToGame(
        gameId: String, player: Player
    ): Result<String> {
        return try {
            val gameRef = gamesCollection.document(gameId);
            var generatedId = ""
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(gameRef)

                if (!snapshot.exists()) {
                    throw IllegalStateException()
                }

                val gameDto =
                    snapshot.toObject(GameDto::class.java) ?: throw IllegalStateException()

                if (gameDto.status == GameStatus.FINISHED.name) {
                    throw IllegalStateException()
                }


                val currentCounter = snapshot.getLong("counter") ?: 0
                val newPlayerId = (currentCounter + 1).toString()
                generatedId = newPlayerId

                val playerWithId = player.copy(id = newPlayerId)
                val playerDto = playerWithId.toDto()

                transaction.update(gameRef, "counter", currentCounter + 1)
                transaction.update(gameRef, "players", FieldValue.arrayUnion(playerDto))

                Unit

            }.await()
            Result.success(generatedId)
        } catch (e: Exception) {
            Log.e("FirebaseGateway", "Failed to add player to game", e)
            Result.failure(e)
        }
    }

    override suspend fun updatePlayer(
        gameId: String, player: Player
    ): Result<Unit> = try {
        val gameRef = gamesCollection.document(gameId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(gameRef)
            val gameDto = snapshot.toObject(GameDto::class.java)
                ?: throw IllegalStateException("Game not found")
            val currentPlayers = gameDto.toEntity()?.players ?: emptyList()
            val updatedPlayers = currentPlayers.map { existingPlayer ->
                if (existingPlayer.id == player.id) player else existingPlayer
            }
            val updatedPlayerDtos = updatedPlayers.map { it.toDto() }
            transaction.update(gameRef, "players", updatedPlayerDtos)
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FirebaseGateway", "Failed to update player", e)
        Result.failure(e)
    }

    override suspend fun removePlayerFromGame(
        gameId: String, playerId: String
    ): Result<Unit> = try {
        gamesCollection.document(gameId).update("players", FieldValue.arrayRemove(playerId)).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FirebaseGateway", "Failed to remove player from game", e)
        Result.failure(e)
    }

    override fun getPlayer(
        gameId: String, playerId: String
    ): Flow<Player?> = callbackFlow {
        val listener = gamesCollection.document(gameId).addSnapshotListener { snapshotFlow, error ->
            if (error != null) {
                Log.e("FirebaseGateway", "Failed to get player", error)
                close(error)
                return@addSnapshotListener
            }
            val gameDto = snapshotFlow?.toObject(GameDto::class.java)
            val game = gameDto?.toEntity()
            val player = game?.players?.find { it.id == playerId }
            trySend(player)
        }
        awaitClose {
            listener.remove()
        }


    }

    override fun getPlayersInGame(gameId: String): Flow<List<Player>?> = callbackFlow {
        val listener = gamesCollection.document(gameId).addSnapshotListener { snapshotFlow, error ->
            if (error != null) {
                Log.e("FirebaseGateway", "Failed to get players", error)
                close(error)
                return@addSnapshotListener
            }
            val gameDto = snapshotFlow?.toObject(GameDto::class.java)
            val game = gameDto?.toEntity()
            val players = game?.players
            trySend(players)
        }
        awaitClose { listener.remove() }

    }

    override suspend fun createGame(game: Game): Result<String> = try {
        val docRef = gamesCollection.document()
        val gameWithId = game.copy(id = docRef.id)
        val gameDto = gameWithId.toDto()
        docRef.set(gameDto).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Log.e("FirebaseGateway", "Failed to create game", e)
        Result.failure(e)

    }

    override fun getGameById(gameId: String): Flow<Game?> = callbackFlow {
        val listener = gamesCollection.document(gameId).addSnapshotListener { snapshotFlow, error ->
            if (error != null) {
                Log.e("FirebaseGateway", "Failed to get game", error)
                close(error)
                return@addSnapshotListener
            }
            val gameDto = snapshotFlow?.toObject(GameDto::class.java)
            val game = gameDto?.toEntity()
            trySend(game)
        }
        awaitClose {
            listener.remove()
        }
    }.catch { e ->
        Log.e("FirebaseGateway", "Failed to get game", e)
        emit(null)
    }

    override suspend fun updateGame(game: Game): Result<Unit> = try {
        val gameDto = game.toDto()
        gamesCollection.document(game.id).set(gameDto).await()
        Log.d("FirebaseGateway", "Game updated: $game")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FirebaseGateway", "Failed to update game", e)
        Result.failure(e)
    }

    override suspend fun deleteGame(gameId: String): Result<Unit> = try {
        gamesCollection.document(gameId).delete().await()
        Log.d("FirebaseGateway", "Game deleted: $gameId")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FirebaseGateway", "Failed to delete game", e)
        Result.failure(e)
    }

    override fun getGamesByStatus(status: GameStatus): Flow<List<Game>> = callbackFlow {
        val listener = gamesCollection.whereEqualTo("status", status.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseGateway", "Error observing games with status $status", error)
                    close(error)
                    return@addSnapshotListener
                }
                val games = snapshot?.documents?.mapNotNull {
                    it.toObject(GameDto::class.java)?.toEntity()
                } ?: emptyList()
                trySend(games)
            }
        awaitClose {
            listener.remove()
        }
    }

    override suspend fun updateGameStatus(
        gameId: String, status: GameStatus
    ): Result<Unit> = try {
        gamesCollection.document(gameId).update("status", status.name).await()
        Result.success(Unit)

    } catch (e: Exception) {
        Log.e("FirebaseGateway", "Failed to update game status", e)
        Result.failure(e)
    }

    override suspend fun endGame(
        gameId: String, winner: PlayerRole
    ): Result<Unit> = try {
        gamesCollection.document(gameId).update("gameWinner", winner.name,
                                                "status", GameStatus.FINISHED).await()
        Result.success(Unit)

    } catch (e: Exception) {
        Log.e("FirebaseGateway", "Failed to update game status", e)
        Result.failure(e)
    }
}