package com.uvg.mypokedex.data.repository

import com.uvg.mypokedex.data.remote.RemoteDataSource
import com.uvg.mypokedex.data.remote.dto.PokemonDetailResponse
import com.uvg.mypokedex.data.remote.dto.PokemonListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PokemonRepository {
    private val api = RemoteDataSource.api

    suspend fun getPokemonList(limit: Int, offset: Int): Result<PokemonListResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getPokemonList(limit, offset)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getPokemonDetail(name: String): Result<PokemonDetailResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getPokemonDetail(name)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
