package com.uvg.mypokedex.ui.features.favorites

import com.uvg.mypokedex.data.remote.dto.FavoritePokemonDto

sealed interface FavoritesUIState {
    data object Loading : FavoritesUIState
    data class Success(val favorites: List<FavoritePokemonDto>) : FavoritesUIState
    data class Error(val message: String) : FavoritesUIState
    data object Empty : FavoritesUIState
}