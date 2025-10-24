package com.uvg.mypokedex.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.mypokedex.data.remote.dto.PokemonDetailResponse
import com.uvg.mypokedex.data.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val pokemon: PokemonDetailResponse) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

class PokemonDetailViewModel : ViewModel() {
    private val repository = PokemonRepository()
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadPokemon(name: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            val result = repository.getPokemonDetail(name)
            result.onSuccess {
                _uiState.value = DetailUiState.Success(it)
            }.onFailure {
                _uiState.value = DetailUiState.Error("No se pudo cargar el Pokémon.")
            }
        }
    }
}
