package com.uvg.mypokedex.ui.features.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.mypokedex.data.remote.dto.PokemonResult
import com.uvg.mypokedex.data.repository.PokemonRepository
import com.uvg.mypokedex.ui.search.SortOption
import kotlinx.coroutines.launch
import com.uvg.mypokedex.data.repository.AuthRepository
import com.uvg.mypokedex.data.repository.FavoritesRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PokemonRepository.create(application)
    private val authRepository = AuthRepository()
    private val favoritesRepository = FavoritesRepository()

    private val allPokemons = mutableListOf<PokemonResult>()
    private val _pokemons = mutableStateListOf<PokemonResult>()
    val pokemons: SnapshotStateList<PokemonResult> get() = _pokemons

    // Favoritos ahora vienen de Firebase
    private val _favoritePokemons = mutableStateListOf<Int>()

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

    init {
        // Observar favoritos desde Firebase
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch

            favoritesRepository.observeFavorites(userId).collect { favorites ->
                _favoritePokemons.clear()
                _favoritePokemons.addAll(favorites.map { it.pokemonId })
                applyFilters()
            }
        }
    }

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
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            val pokemonId = extractIdFromUrl(
                allPokemons.find { it.name == pokemonName }?.url ?: return@launch
            ).toInt()

            val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"

            if (_favoritePokemons.contains(pokemonId)) {
                favoritesRepository.removeFavorite(userId, pokemonId)
            } else {
                favoritesRepository.addFavorite(userId, pokemonId, pokemonName, imageUrl)
            }
        }
    }

    fun isFavorite(pokemonName: String): Boolean {
        val pokemonId = extractIdFromUrl(
            allPokemons.find { it.name == pokemonName }?.url ?: return false
        ).toIntOrNull() ?: return false

        return _favoritePokemons.contains(pokemonId)
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
            list = list.filter { pokemon ->
                val id = extractIdFromUrl(pokemon.url).toIntOrNull()
                id != null && _favoritePokemons.contains(id)
            }
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

        // Ordenar favoritos al principio
        list = list.sortedByDescending { pokemon ->
            val id = extractIdFromUrl(pokemon.url).toIntOrNull()
            id != null && _favoritePokemons.contains(id)
        }

        _pokemons.clear()
        _pokemons.addAll(list)
    }

    private fun extractIdFromUrl(url: String): String {
        return url.trimEnd('/').split("/").last()
    }
}