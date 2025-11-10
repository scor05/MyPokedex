package com.uvg.mypokedex.ui.pokedex

import com.uvg.mypokedex.data.remote.dto.PokemonResult

sealed class PokedexUiState {
    object Loading : PokedexUiState()
    data class Success(val pokemons: List<PokemonResult>) : PokedexUiState()
    data class Error(val message: String) : PokedexUiState()
}
