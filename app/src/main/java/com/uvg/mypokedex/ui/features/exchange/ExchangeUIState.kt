package com.uvg.mypokedex.ui.features.exchange


import com.uvg.mypokedex.data.remote.dto.ExchangeDto
import com.uvg.mypokedex.data.remote.dto.FavoritePokemonDto

sealed interface ExchangeUIState {
    data object Idle : ExchangeUIState
    data object Loading : ExchangeUIState

    data class SelectingPokemon(
        val myFavorites: List<FavoritePokemonDto>,
        val selectedMyPokemon: FavoritePokemonDto? = null
    ) : ExchangeUIState

    data class SearchingUser(
        val myPokemon: FavoritePokemonDto
    ) : ExchangeUIState

    data class SelectingTargetPokemon(
        val myPokemon: FavoritePokemonDto,
        val targetUserId: String,
        val targetUserName: String,
        val targetFavorites: List<FavoritePokemonDto>,
        val selectedTargetPokemon: FavoritePokemonDto? = null
    ) : ExchangeUIState

    data class ConfirmingExchange(
        val myPokemon: FavoritePokemonDto,
        val targetUserId: String,
        val targetUserName: String,
        val targetPokemon: FavoritePokemonDto
    ) : ExchangeUIState

    data class WaitingForAcceptance(
        val exchange: ExchangeDto
    ) : ExchangeUIState

    data class ExchangeCompleted(
        val exchange: ExchangeDto
    ) : ExchangeUIState

    data class Error(val message: String) : ExchangeUIState
}

// Estado para manejar propuestas entrantes
sealed interface IncomingExchangeState {
    data object NoExchanges : IncomingExchangeState
    data class HasPendingExchanges(val exchanges: List<ExchangeDto>) : IncomingExchangeState
}