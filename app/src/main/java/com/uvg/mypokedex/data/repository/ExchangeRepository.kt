package com.uvg.mypokedex.data.repository

import com.uvg.mypokedex.data.remote.dto.ExchangeDto
import com.uvg.mypokedex.data.remote.firebase.FirebaseExchangeService
import kotlinx.coroutines.flow.Flow

class ExchangeRepository(
    private val exchangeService: FirebaseExchangeService = FirebaseExchangeService()
) {

    // Observar intercambios pendientes
    fun observePendingExchanges(userId: String): Flow<List<ExchangeDto>> {
        return exchangeService.observePendingExchanges(userId)
    }

    // Observar un intercambio específico
    fun observeExchange(exchangeId: String): Flow<ExchangeDto?> {
        return exchangeService.observeExchange(exchangeId)
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
        return exchangeService.createExchangeProposal(
            userAId, userAName, userBId, userBName,
            pokemonAId, pokemonAName, pokemonBId, pokemonBName
        )
    }

    // Aceptar intercambio
    suspend fun acceptExchange(exchangeId: String): Result<Unit> {
        return exchangeService.acceptExchange(exchangeId)
    }

    // Rechazar intercambio
    suspend fun rejectExchange(exchangeId: String): Result<Unit> {
        return exchangeService.rejectExchange(exchangeId)
    }

    // Cancelar intercambio
    suspend fun cancelExchange(exchangeId: String): Result<Unit> {
        return exchangeService.cancelExchange(exchangeId)
    }

    // Obtener intercambio
    suspend fun getExchange(exchangeId: String): Result<ExchangeDto?> {
        return exchangeService.getExchange(exchangeId)
    }

    // Buscar usuario
    suspend fun findUser(searchQuery: String): Result<List<Pair<String, String>>> {
        return exchangeService.findUserByIdOrName(searchQuery)
    }
}