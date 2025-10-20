package com.uvg.mypokedex.data.remote

import com.uvg.mypokedex.data.remote.dto.PokemonDetailDto
import com.uvg.mypokedex.data.remote.dto.PokemonListResponse
import retrofit2.HttpException
import java.io.IOException

class RemoteDataSource(private val api: PokemonApi) {

    suspend fun fetchPokemonList(limit: Int, offset: Int): Result<PokemonListResponse> {
        return try {
            val response = api.getPokemonList(limit, offset)
            Result.success(response)
        } catch (e: IOException) {
            // Error de red (sin conexión, timeout)
            Result.failure(Exception("No se pudo conectar al servidor. Verifica tu conexión."))
        } catch (e: HttpException) {
            // Error HTTP (404, 500, etc.)
            Result.failure(Exception("Error del servidor (${e.code()})"))
        } catch (e: Exception) {
            // Cualquier otro error
            Result.failure(Exception("Error inesperado: ${e.localizedMessage}"))
        }
    }

    suspend fun fetchPokemonDetail(name: String): Result<PokemonDetailDto> {
        return try {
            val response = api.getPokemonDetail(name)
            Result.success(response)
        } catch (e: IOException) {
            Result.failure(Exception("Sin conexión a internet."))
        } catch (e: HttpException) {
            Result.failure(Exception("Pokémon no encontrado (${e.code()})"))
        } catch (e: Exception) {
            Result.failure(Exception("Error inesperado: ${e.localizedMessage}"))
        }
    }
}
