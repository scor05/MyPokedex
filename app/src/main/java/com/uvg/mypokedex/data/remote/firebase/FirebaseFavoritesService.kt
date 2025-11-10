package com.uvg.mypokedex.data.remote.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.uvg.mypokedex.data.remote.dto.FavoritePokemonDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseFavoritesService {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Observar favoritos en tiempo real
    fun observeFavorites(userId: String): Flow<List<FavoritePokemonDto>> = callbackFlow {
        val listener: ListenerRegistration = firestore
            .collection("users")
            .document(userId)
            .collection("favorites")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val favorites = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FavoritePokemonDto::class.java)
                } ?: emptyList()

                trySend(favorites)
            }

        awaitClose { listener.remove() }
    }

    // Agregar Pokémon a favoritos
    suspend fun addFavorite(
        userId: String,
        pokemonId: Int,
        pokemonName: String,
        imageUrl: String
    ): Result<Unit> {
        return try {
            val favorite = FavoritePokemonDto(
                pokemonId = pokemonId,
                pokemonName = pokemonName,
                imageUrl = imageUrl,
                addedAt = System.currentTimeMillis()
            )

            // Agregar a la subcolección de favoritos
            firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(pokemonId.toString())
                .set(favorite)
                .await()

            // También actualizar el array de IDs en el documento del usuario
            firestore.collection("users")
                .document(userId)
                .update("favoritePokemonIds", FieldValue.arrayUnion(pokemonId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Remover Pokémon de favoritos
    suspend fun removeFavorite(userId: String, pokemonId: Int): Result<Unit> {
        return try {
            // Remover de la subcolección
            firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(pokemonId.toString())
                .delete()
                .await()

            // También remover del array de IDs
            firestore.collection("users")
                .document(userId)
                .update("favoritePokemonIds", FieldValue.arrayRemove(pokemonId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verificar si un Pokémon está en favoritos
    suspend fun isFavorite(userId: String, pokemonId: Int): Result<Boolean> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(pokemonId.toString())
                .get()
                .await()

            Result.success(document.exists())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener todos los favoritos (sin tiempo real)
    suspend fun getFavorites(userId: String): Result<List<FavoritePokemonDto>> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .await()

            val favorites = snapshot.documents.mapNotNull { doc ->
                doc.toObject(FavoritePokemonDto::class.java)
            }

            Result.success(favorites)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener un favorito específico
    suspend fun getFavorite(userId: String, pokemonId: Int): Result<FavoritePokemonDto?> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(pokemonId.toString())
                .get()
                .await()

            val favorite = document.toObject(FavoritePokemonDto::class.java)
            Result.success(favorite)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Limpiar todos los favoritos (útil para testing)
    suspend fun clearAllFavorites(userId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .await()

            // Eliminar cada documento
            snapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            // Limpiar el array de IDs
            firestore.collection("users")
                .document(userId)
                .update("favoritePokemonIds", emptyList<Int>())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Contar favoritos
    suspend fun getFavoritesCount(userId: String): Result<Int> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .await()

            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}