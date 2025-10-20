package com.uvg.mypokedex.ui.features.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.mypokedex.data.Pokemon
import com.uvg.mypokedex.data.remote.NetworkModule
import com.uvg.mypokedex.data.remote.RemoteDataSource
import com.uvg.mypokedex.data.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PokemonDetailUiState(
    val isLoading: Boolean = false,
    val pokemon: Pokemon? = null,
    val error: String? = null
)

class PokemonDetailViewModel : ViewModel() {

    private val repository = PokemonRepository(
        remoteDataSource = RemoteDataSource(NetworkModule.api)
    )

    private val _uiState = MutableStateFlow(PokemonDetailUiState())
    val uiState: StateFlow<PokemonDetailUiState> = _uiState

    fun loadPokemonDetail(name: String) {
        _uiState.value = PokemonDetailUiState(isLoading = true)

        viewModelScope.launch {
            val result = repository.getPokemonDetail(name)
            result.fold(
                onSuccess = { pokemon ->
                    _uiState.value = PokemonDetailUiState(pokemon = pokemon)
                },
                onFailure = { e ->
                    _uiState.value = PokemonDetailUiState(error = e.message)
                }
            )
        }
    }
}
