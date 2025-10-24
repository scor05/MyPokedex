package com.uvg.mypokedex.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.uvg.mypokedex.data.remote.dto.PokemonResult
import com.uvg.mypokedex.data.repository.PokemonRepository
import com.uvg.mypokedex.ui.search.SortOption
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = PokemonRepository()

    private val allPokemons = mutableListOf<PokemonResult>()
    private val _pokemons = mutableStateListOf<PokemonResult>()
    val pokemons: SnapshotStateList<PokemonResult> get() = _pokemons

    private val _favoritePokemons = mutableStateListOf<String>()

    var favoritesToggle by mutableStateOf(false)
        private set

    var sortOption by mutableStateOf(SortOption.Numero)
        private set

    var ascending by mutableStateOf(true)
        private set

    private var offset = 0
    private val limit = 20
    private var endReached = false
    var loading by mutableStateOf(false)
        private set

    fun loadMorePokemon() {
        if (loading || endReached) return
        loading = true

        viewModelScope.launch {
            val result = repository.getPokemonList(limit, offset)
            result.onSuccess { response ->
                if (response.results.isEmpty()) {
                    endReached = true
                } else {
                    allPokemons.addAll(response.results)
                    offset += limit
                    applyFilters()
                }
            }.onFailure {
                endReached = true
            }
            loading = false
        }
    }

    fun toggleFavorite(pokemonName: String) {
        if (_favoritePokemons.contains(pokemonName)) {
            _favoritePokemons.remove(pokemonName)
        } else {
            _favoritePokemons.add(pokemonName)
        }
        applyFilters()
    }

    fun isFavorite(pokemonName: String): Boolean {
        return _favoritePokemons.contains(pokemonName)
    }

    fun setSortOptionCustom(value: SortOption) {
        sortOption = value
        applyFilters()
    }

    fun setAscendingCustom(value: Boolean) {
        ascending = value
        applyFilters()
    }

    fun setFavoritesOnly(value: Boolean) {
        favoritesToggle = value
        applyFilters()
    }

    private fun applyFilters() {
        var list = allPokemons.toList()

        if (favoritesToggle) {
            list = list.filter { _favoritePokemons.contains(it.name) }
        }

        list = when (sortOption) {
            SortOption.Numero -> {
                if (ascending) list.sortedBy { extractIdFromUrl(it.url).toInt() }
                else list.sortedByDescending { extractIdFromUrl(it.url).toInt() }
            }
            SortOption.Nombre -> {
                if (ascending) list.sortedBy { it.name }
                else list.sortedByDescending { it.name }
            }
        }

        list = list.sortedByDescending { _favoritePokemons.contains(it.name) }

        _pokemons.clear()
        _pokemons.addAll(list)
    }

    private fun extractIdFromUrl(url: String): String {
        return url.trimEnd('/').split("/").last()
    }
}
