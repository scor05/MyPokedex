package com.uvg.mypokedex.ui.features.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.mypokedex.data.Pokemon
import com.uvg.mypokedex.data.remote.NetworkModule
import com.uvg.mypokedex.data.remote.RemoteDataSource
import com.uvg.mypokedex.data.repository.PokemonRepository
import com.uvg.mypokedex.ui.search.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = PokemonRepository(
        remoteDataSource = RemoteDataSource(NetworkModule.api)
    )

    private val _uiState = MutableStateFlow(PokemonListUiState())
    val uiState: StateFlow<PokemonListUiState> = _uiState

    private val _favoritePokemons = mutableSetOf<String>()
    fun toggleFavorite(name: String) {
        if (_favoritePokemons.contains(name)) _favoritePokemons.remove(name)
        else _favoritePokemons.add(name)
    }
    fun isFavorite(name: String) = _favoritePokemons.contains(name)

    var sortOption: SortOption = SortOption.Numero
        private set
    var ascending: Boolean = true
        private set
    var favoritesToggle: Boolean = false
        private set

    var showDialog by mutableStateOf(false)
        private set

    fun toggleDialog(value: Boolean) {
        showDialog = value
    }

    fun setSortOptionCustom(value: SortOption) { sortOption = value }
    fun setAscendingCustom(value: Boolean) { ascending = value }
    fun setFavoritesOnly(value: Boolean) { favoritesToggle = value }

    init {
        loadMorePokemon()
    }

    fun getPokemon(name: String): Pokemon{
        return _uiState.value.pokemons.find { it.name == name }!!
    }

    fun loadMorePokemon() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.endReached) return

        _uiState.value = currentState.copy(isLoading = true)

        viewModelScope.launch {
            val result = repository.getPokemonList(currentState.page)

            result.fold(
                onSuccess = { newList ->
                    val updatedList = currentState.pokemons + newList
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        pokemons = updatedList,
                        error = null,
                        page = currentState.page + 1,
                        endReached = newList.isEmpty()
                    )
                },
                onFailure = { e ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = e.message ?: "Error desconocido"
                    )
                }
            )
        }
    }

    fun retry() {
        _uiState.value = _uiState.value.copy(error = null)
        loadMorePokemon()
    }

    fun getVisiblePokemons(): List<Pokemon> {
        var list = _uiState.value.pokemons

        if (favoritesToggle) list = list.filter { isFavorite(it.name) }

        list = when (sortOption) {
            SortOption.Numero -> if (ascending) list.sortedBy { it.id } else list.sortedByDescending { it.id }
            SortOption.Nombre -> if (ascending) list.sortedBy { it.name } else list.sortedByDescending { it.name }
        }

        return list
    }
}
