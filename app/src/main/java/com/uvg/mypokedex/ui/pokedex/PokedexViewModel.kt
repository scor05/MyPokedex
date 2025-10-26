package com.uvg.mypokedex.ui.pokedex

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.mypokedex.data.prefs.OrderType
import com.uvg.mypokedex.data.remote.dto.PokemonResult
import com.uvg.mypokedex.data.repository.PokemonRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PokedexViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PokemonRepository.create(application)

    private val _uiState = MutableStateFlow<PokedexUiState>(PokedexUiState.Loading)
    val uiState: StateFlow<PokedexUiState> = _uiState.asStateFlow()

    private var offset = 0
    private val limit = 20
    private var collectJob: Job? = null
    private var isRefreshing = false

    init {
        observeCache()
        loadPokemons()
    }

    fun loadPokemons() {
        if (isRefreshing) return
        isRefreshing = true
        _uiState.value = PokedexUiState.Loading
        viewModelScope.launch {
            runCatching { repository.refreshPage(limit = limit, offset = offset) }
                .onSuccess {
                    offset += limit
                    isRefreshing = false
                }
                .onFailure {
                    isRefreshing = false
                    _uiState.value = PokedexUiState.Error("No se pudo cargar la lista")
                }
        }
    }

    fun changeOrder(order: OrderType) {
        viewModelScope.launch { repository.setOrder(order) }
    }

    private fun observeCache() {
        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            repository.pokedex.collectLatest { cachedList ->
                val mapped: List<PokemonResult> = cachedList.map { c ->
                    PokemonResult(
                        name = c.name,
                        url = "https://pokeapi.co/api/v2/pokemon/${c.id}/"
                    )
                }
                _uiState.value = PokedexUiState.Success(mapped)
            }
        }
    }
}