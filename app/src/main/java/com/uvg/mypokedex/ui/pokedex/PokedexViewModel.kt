package com.uvg.mypokedex.ui.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.mypokedex.data.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PokedexViewModel : ViewModel() {
    private val repository = PokemonRepository()
    private val _uiState = MutableStateFlow<PokedexUiState>(PokedexUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var offset = 0
    private val limit = 20
    private val loadedPokemons = mutableListOf<com.uvg.mypokedex.data.remote.dto.PokemonResult>()

    init {
        loadPokemons()
    }

    fun loadPokemons() {
        viewModelScope.launch {
            _uiState.value = PokedexUiState.Loading
            val result = repository.getPokemonList(limit, offset)
            result.onSuccess { response ->
                loadedPokemons.addAll(response.results)
                _uiState.value = PokedexUiState.Success(loadedPokemons.toList())
                offset += limit
            }.onFailure {
                _uiState.value = PokedexUiState.Error("No se pudo cargar la lista")
            }
        }
    }
}
