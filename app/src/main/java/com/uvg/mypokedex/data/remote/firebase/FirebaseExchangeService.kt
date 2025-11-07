package com.uvg.mypokedex.data.remote.firebase

import com.google.firebase.firestore.*
import com.uvg.mypokedex.data.remote.dto.ExchangeDto
import com.uvg.mypokedex.data.remote.dto.ExchangeStatus
import com.uvg.mypokedex.data.remote.dto.FavoritePokemonDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseExchangeService {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Observar intercambios pendientes de un usuario
    fun observePendingExchanges(userId: String): Flow<List<ExchangeDto>> = callbackFlow {
        val listener: ListenerRegistration = firestore
            .collection("exchanges")
            .whereEqualTo("userBId", userId)
            .whereEqualTo("status", ExchangeStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val exchanges = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ExchangeDto::class.java)
                } ?: emptyList()

                trySend(exchanges)
            }

        awaitClose { listener.remove() }
    }

    // Observar un intercambio específico en tiempo real
    fun observeExchange(exchangeId: String): Flow<ExchangeDto?> = callbackFlow {
        val listener: ListenerRegistration = firestore
            .collection("exchanges")
            .document(exchangeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val exchange = snapshot?.toObject(ExchangeDto::class.java)
                trySend(exchange)
            }

        awaitClose { listener.remove() }
    }

    // Crear propuesta de intercambio
    suspend fun createExchangeProposal(
        userAId: String,
        userAName: String,
        userBId: String,
        userBName: String,
        pokemonAId: Int,
        pokemonAName: String,
        pokemonBId: Int,
        pokemonBName: String
    ): Result<ExchangeDto> {
        return try {
            val exchangeId = UUID.randomUUID().toString()

            val exchange = ExchangeDto(
                exchangeId = exchangeId,
                userAId = userAId,
                userAName = userAName,
                userBId = userBId,
                userBName = userBName,
                pokemonAId = pokemonAId,
                pokemonAName = pokemonAName,
                pokemonBId = pokemonBId,
                pokemonBName = pokemonBName,
                status = ExchangeStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )

            firestore.collection("exchanges")
                .document(exchangeId)
                .set(exchange)
                .await()

            Result.success(exchange)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Aceptar intercambio y ejecutar transacción atómica
    suspend fun acceptExchange(exchangeId: String): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                // 1. Obtener el documento del intercambio
                val exchangeRef = firestore.collection("exchanges").document(exchangeId)
                val exchangeSnapshot = transaction.get(exchangeRef)
                val exchange = exchangeSnapshot.toObject(ExchangeDto::class.java)
                    ?: throw Exception("Intercambio no encontrado")

                // Verificar que esté en estado PENDING
                if (exchange.status != ExchangeStatus.PENDING) {
                    throw Exception("El intercambio ya no está pendiente")
                }

                // 2. Referencias a los favoritos
                val userAFavoriteARef = firestore
                    .collection("users")
                    .document(exchange.userAId)
                    .collection("favorites")
                    .document(exchange.pokemonAId.toString())

                val userAFavoriteBRef = firestore
                    .collection("users")
                    .document(exchange.userAId)
                    .collection("favorites")
                    .document(exchange.pokemonBId.toString())

                val userBFavoriteBRef = firestore
                    .collection("users")
                    .document(exchange.userBId)
                    .collection("favorites")
                    .document(exchange.pokemonBId.toString())

                val userBFavoriteARef = firestore
                    .collection("users")
                    .document(exchange.userBId)
                    .collection("favorites")
                    .document(exchange.pokemonAId.toString())

                // 3. Obtener los favoritos actuales
                val favoritePokemonA = transaction.get(userAFavoriteARef)
                    .toObject(FavoritePokemonDto::class.java)
                    ?: throw Exception("Pokémon A no encontrado en favoritos de Usuario A")

                val favoritePokemonB = transaction.get(userBFavoriteBRef)
                    .toObject(FavoritePokemonDto::class.java)
                    ?: throw Exception("Pokémon B no encontrado en favoritos de Usuario B")

                // 4. INTERCAMBIO ATÓMICO
                // Usuario A: elimina pokemonA, recibe pokemonB
                transaction.delete(userAFavoriteARef)
                transaction.set(userAFavoriteBRef, favoritePokemonB)

                // Usuario B: elimina pokemonB, recibe pokemonA
                transaction.delete(userBFavoriteBRef)
                transaction.set(userBFavoriteARef, favoritePokemonA)

                // 5. Actualizar arrays de IDs
                val userARef = firestore.collection("users").document(exchange.userAId)
                val userBRef = firestore.collection("users").document(exchange.userBId)

                transaction.update(userARef,
                    "favoritePokemonIds",
                    FieldValue.arrayRemove(exchange.pokemonAId)
                )
                transaction.update(userARef,
                    "favoritePokemonIds",
                    FieldValue.arrayUnion(exchange.pokemonBId)
                )

                transaction.update(userBRef,
                    "favoritePokemonIds",
                    FieldValue.arrayRemove(exchange.pokemonBId)
                )
                transaction.update(userBRef,
                    "favoritePokemonIds",
                    FieldValue.arrayUnion(exchange.pokemonAId)
                )

                // 6. Actualizar estado del intercambio
                transaction.update(exchangeRef, mapOf(
                    "status" to ExchangeStatus.COMPLETED.name,
                    "completedAt" to System.currentTimeMillis()
                ))

            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Rechazar intercambio
    suspend fun rejectExchange(exchangeId: String): Result<Unit> {
        return try {
            firestore.collection("exchanges")
                .document(exchangeId)
                .update(mapOf(
                    "status" to ExchangeStatus.CANCELLED.name,
                    "completedAt" to System.currentTimeMillis()
                ))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cancelar intercambio (por timeout o por usuario)
    suspend fun cancelExchange(exchangeId: String): Result<Unit> {
        return try {
            firestore.collection("exchanges")
                .document(exchangeId)
                .update(mapOf(
                    "status" to ExchangeStatus.CANCELLED.name,
                    "completedAt" to System.currentTimeMillis()
                ))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener intercambio por ID
    suspend fun getExchange(exchangeId: String): Result<ExchangeDto?> {
        return try {
            val document = firestore.collection("exchanges")
                .document(exchangeId)
                .get()
                .await()

            val exchange = document.toObject(ExchangeDto::class.java)
            Result.success(exchange)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Buscar usuario por ID o nombre
    suspend fun findUserByIdOrName(searchQuery: String): Result<List<Pair<String, String>>> {
        return try {
            // Buscar por UID exacto
            val userByIdSnapshot = firestore.collection("users")
                .document(searchQuery)
                .get()
                .await()

            if (userByIdSnapshot.exists()) {
                val displayName = userByIdSnapshot.getString("displayName") ?: "Usuario"
                return Result.success(listOf(searchQuery to displayName))
            }

            // Buscar por nombre (case insensitive)
            val usersByNameSnapshot = firestore.collection("users")
                .whereEqualTo("displayName", searchQuery)
                .get()
                .await()

            val users = usersByNameSnapshot.documents.mapNotNull { doc ->
                val uid = doc.id
                val displayName = doc.getString("displayName") ?: "Usuario"
                uid to displayName
            }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Limpiar intercambios antiguos (opcional, para limpieza)
    suspend fun cleanupOldExchanges(olderThanMillis: Long = 24 * 60 * 60 * 1000): Result<Int> {
        return try {
            val cutoffTime = System.currentTimeMillis() - olderThanMillis

            val snapshot = firestore.collection("exchanges")
                .whereLessThan("createdAt", cutoffTime)
                .whereIn("status", listOf(
                    ExchangeStatus.COMPLETED.name,
                    ExchangeStatus.CANCELLED.name,
                    ExchangeStatus.TIMEOUT.name
                ))
                .get()
                .await()

            var deletedCount = 0
            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
                deletedCount++
            }

            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}