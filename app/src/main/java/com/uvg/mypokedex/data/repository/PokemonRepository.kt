package com.uvg.mypokedex.data.repository

import com.uvg.mypokedex.data.Pokemon
import com.uvg.mypokedex.data.mapper.toDomain
import com.uvg.mypokedex.data.remote.RemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PokemonRepository(private val remoteDataSource: RemoteDataSource) {

    private val pageSize = 20

    suspend fun getPokemonList(page: Int): Result<List<Pokemon>> = withContext(Dispatchers.IO) {
        val offset = (page - 1) * pageSize

        val listResult = remoteDataSource.fetchPokemonList(limit = pageSize, offset = offset)
        listResult.fold(
            onSuccess = { response ->
                val pokemons = response.results.mapNotNull { item ->
                    val detailResult = remoteDataSource.fetchPokemonDetail(item.name)
                    detailResult.getOrNull()?.toDomain()
                }
                Result.success(pokemons)
            },
            onFailure = { e ->
                Result.failure(e)
            }
        )
    }

    suspend fun getPokemonDetail(name: String): Result<Pokemon> = withContext(Dispatchers.IO) {
        val detailResult = remoteDataSource.fetchPokemonDetail(name)
        detailResult.fold(
            onSuccess = { Result.success(it.toDomain()) },
            onFailure = { Result.failure(it) }
        )
    }
}
