package com.uvg.mypokedex.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.uvg.mypokedex.data.remote.dto.ExchangeDto
import com.uvg.mypokedex.data.remote.dto.ExchangeStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ExchangeRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val favoritesRepo = FavoritesRepository()

    // Crear una propuesta de intercambio
    suspend fun createExchange(
        userAId: String,
        userAName: String,
        userBId: String,
        userBName: String,
        pokemonAId: Int,
        pokemonAName: String,
        pokemonBId: Int,
        pokemonBName: String
    ): Result<String> {
        return try {
            val exchangeId = firestore.collection("exchanges").document().id
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

            Result.success(exchangeId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Observar intercambios pendientes para un usuario
    fun getPendingExchangesFlow(userId: String): Flow<List<ExchangeDto>> = callbackFlow {
        val listener = firestore.collection("exchanges")
            .whereEqualTo("userBId", userId)
            .whereEqualTo("status", ExchangeStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val exchanges = snapshot?.documents?.mapNotNull {
                    it.toObject(ExchangeDto::class.java)
                } ?: emptyList()

                trySend(exchanges)
            }

        awaitClose { listener.remove() }
    }

    // Aceptar un intercambio
    suspend fun acceptExchange(exchangeId: String): Result<Unit> {
        return try {
            // Actualizar estado a ACCEPTED
            firestore.collection("exchanges")
                .document(exchangeId)
                .update(
                    mapOf(
                        "status" to ExchangeStatus.ACCEPTED.name,
                        "completedAt" to System.currentTimeMillis()
                    )
                )
                .await()

            // Obtener detalles del intercambio
            val exchange = firestore.collection("exchanges")
                .document(exchangeId)
                .get()
                .await()
                .toObject(ExchangeDto::class.java)
                ?: throw Exception("Intercambio no encontrado")

            // Realizar el intercambio de favoritos usando Firestore Transaction
            firestore.runTransaction { transaction ->
                // Remover Pokémon A de favoritos de Usuario A
                val favARef = firestore.collection("users")
                    .document(exchange.userAId)
                    .collection("favorites")
                    .document(exchange.pokemonAId.toString())

                // Remover Pokémon B de favoritos de Usuario B
                val favBRef = firestore.collection("users")
                    .document(exchange.userBId)
                    .collection("favorites")
                    .document(exchange.pokemonBId.toString())

                // Leer los documentos
                val favA = transaction.get(favARef)
                val favB = transaction.get(favBRef)

                // Eliminar los documentos originales
                transaction.delete(favARef)
                transaction.delete(favBRef)

                // Agregar Pokémon B a Usuario A
                val newFavARef = firestore.collection("users")
                    .document(exchange.userAId)
                    .collection("favorites")
                    .document(exchange.pokemonBId.toString())

                transaction.set(newFavARef, mapOf(
                    "pokemonId" to exchange.pokemonBId,
                    "pokemonName" to exchange.pokemonBName,
                    "imageUrl" to favB.getString("imageUrl"),
                    "addedAt" to System.currentTimeMillis()
                ))

                // Agregar Pokémon A a Usuario B
                val newFavBRef = firestore.collection("users")
                    .document(exchange.userBId)
                    .collection("favorites")
                    .document(exchange.pokemonAId.toString())

                transaction.set(newFavBRef, mapOf(
                    "pokemonId" to exchange.pokemonAId,
                    "pokemonName" to exchange.pokemonAName,
                    "imageUrl" to favA.getString("imageUrl"),
                    "addedAt" to System.currentTimeMillis()
                ))

                // Actualizar estado a COMPLETED
                val exchangeRef = firestore.collection("exchanges").document(exchangeId)
                transaction.update(exchangeRef, "status", ExchangeStatus.COMPLETED.name)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Rechazar un intercambio
    suspend fun rejectExchange(exchangeId: String): Result<Unit> {
        return try {
            firestore.collection("exchanges")
                .document(exchangeId)
                .update(
                    mapOf(
                        "status" to ExchangeStatus.CANCELLED.name,
                        "completedAt" to System.currentTimeMillis()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verificar timeout de intercambios
    suspend fun checkTimeouts(): Result<Int> {
        return try {
            val now = System.currentTimeMillis()
            val timeoutThreshold = 90_000L // 90 segundos

            val pendingExchanges = firestore.collection("exchanges")
                .whereEqualTo("status", ExchangeStatus.PENDING.name)
                .get()
                .await()

            var timeoutCount = 0
            pendingExchanges.documents.forEach { doc ->
                val exchange = doc.toObject(ExchangeDto::class.java)
                if (exchange != null && (now - exchange.createdAt) > timeoutThreshold) {
                    firestore.collection("exchanges")
                        .document(exchange.exchangeId)
                        .update("status", ExchangeStatus.TIMEOUT.name)
                        .await()
                    timeoutCount++
                }
            }

            Result.success(timeoutCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}