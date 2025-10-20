package com.uvg.mypokedex.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.mypokedex.data.Pokemon
import com.uvg.mypokedex.data.remote.NetworkModule
import com.uvg.mypokedex.data.remote.RemoteDataSource
import com.uvg.mypokedex.data.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PokemonListUiState(
    val isLoading: Boolean = false,
    val pokemons: List<Pokemon> = emptyList(),
    val error: String? = null,
    val page: Int = 1,
    val endReached: Boolean = false
)

class PokemonListViewModel : ViewModel() {

    private val repository = PokemonRepository(
        remoteDataSource = RemoteDataSource(NetworkModule.api)
    )

    private val _uiState = MutableStateFlow(PokemonListUiState())
    val uiState: StateFlow<PokemonListUiState> = _uiState

    init {
        loadPokemons()
    }

    fun loadPokemons() {
        if (_uiState.value.isLoading || _uiState.value.endReached) return

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val result = repository.getPokemonList(_uiState.value.page)
            result.fold(
                onSuccess = { newList ->
                    val allPokemons = _uiState.value.pokemons + newList
                    val reachedEnd = newList.isEmpty()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pokemons = allPokemons,
                        error = null,
                        page = _uiState.value.page + 1,
                        endReached = reachedEnd
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Error desconocido"
                    )
                }
            )
        }
    }

    fun retry() {
        _uiState.value = _uiState.value.copy(error = null)
        loadPokemons()
    }
}
