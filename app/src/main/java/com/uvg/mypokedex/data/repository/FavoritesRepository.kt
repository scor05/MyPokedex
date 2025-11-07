package com.uvg.mypokedex.data.repository

import com.uvg.mypokedex.data.remote.dto.FavoritePokemonDto
import com.uvg.mypokedex.data.remote.firebase.FirebaseFavoritesService
import kotlinx.coroutines.flow.Flow

class FavoritesRepository(
    private val favoritesService: FirebaseFavoritesService = FirebaseFavoritesService()
) {

    // Observar favoritos en tiempo real
    fun observeFavorites(userId: String): Flow<List<FavoritePokemonDto>> {
        return favoritesService.observeFavorites(userId)
    }

    // Agregar a favoritos
    suspend fun addFavorite(
        userId: String,
        pokemonId: Int,
        pokemonName: String,
        imageUrl: String
    ): Result<Unit> {
        return favoritesService.addFavorite(userId, pokemonId, pokemonName, imageUrl)
    }

    // Remover de favoritos
    suspend fun removeFavorite(userId: String, pokemonId: Int): Result<Unit> {
        return favoritesService.removeFavorite(userId, pokemonId)
    }

    // Toggle favorito (agregar si no existe, remover si existe)
    suspend fun toggleFavorite(
        userId: String,
        pokemonId: Int,
        pokemonName: String,
        imageUrl: String
    ): Result<Boolean> {
        return try {
            val isFavoriteResult = favoritesService.isFavorite(userId, pokemonId)

            if (isFavoriteResult.isSuccess) {
                val isFavorite = isFavoriteResult.getOrNull() ?: false

                if (isFavorite) {
                    favoritesService.removeFavorite(userId, pokemonId)
                    Result.success(false) // Ahora NO es favorito
                } else {
                    favoritesService.addFavorite(userId, pokemonId, pokemonName, imageUrl)
                    Result.success(true) // Ahora SI es favorito
                }
            } else {
                Result.failure(isFavoriteResult.exceptionOrNull() ?: Exception("Error desconocido"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verificar si es favorito
    suspend fun isFavorite(userId: String, pokemonId: Int): Result<Boolean> {
        return favoritesService.isFavorite(userId, pokemonId)
    }

    // Obtener favoritos (snapshot)
    suspend fun getFavorites(userId: String): Result<List<FavoritePokemonDto>> {
        return favoritesService.getFavorites(userId)
    }

    // Obtener un favorito específico
    suspend fun getFavorite(userId: String, pokemonId: Int): Result<FavoritePokemonDto?> {
        return favoritesService.getFavorite(userId, pokemonId)
    }

    // Contar favoritos
    suspend fun getFavoritesCount(userId: String): Result<Int> {
        return favoritesService.getFavoritesCount(userId)
    }

    // Limpiar favoritos
    suspend fun clearAllFavorites(userId: String): Result<Unit> {
        return favoritesService.clearAllFavorites(userId)
    }
}