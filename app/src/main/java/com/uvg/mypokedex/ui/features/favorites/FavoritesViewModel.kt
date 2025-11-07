package com.uvg.mypokedex.ui.features.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.mypokedex.data.repository.AuthRepository
import com.uvg.mypokedex.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository = FavoritesRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritesUIState>(FavoritesUIState.Loading)
    val uiState: StateFlow<FavoritesUIState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid

            if (userId == null) {
                _uiState.value = FavoritesUIState.Error("Usuario no autenticado")
                return@launch
            }

            _uiState.value = FavoritesUIState.Loading

            favoritesRepository.observeFavorites(userId).collect { favorites ->
                _uiState.value = if (favorites.isEmpty()) {
                    FavoritesUIState.Empty
                } else {
                    FavoritesUIState.Success(favorites)
                }
            }
        }
    }

    fun addFavorite(pokemonId: Int, pokemonName: String, imageUrl: String) {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch

            _isLoading.value = true

            favoritesRepository.addFavorite(userId, pokemonId, pokemonName, imageUrl)
                .onSuccess {
                    println("✅ Pokémon agregado a favoritos: $pokemonName")
                }
                .onFailure { error ->
                    println("❌ Error agregando favorito: ${error.message}")
                    _uiState.value = FavoritesUIState.Error(error.message ?: "Error desconocido")
                }

            _isLoading.value = false
        }
    }

    fun removeFavorite(pokemonId: Int) {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch

            _isLoading.value = true

            favoritesRepository.removeFavorite(userId, pokemonId)
                .onSuccess {
                    println("✅ Pokémon removido de favoritos")
                }
                .onFailure { error ->
                    println("❌ Error removiendo favorito: ${error.message}")
                    _uiState.value = FavoritesUIState.Error(error.message ?: "Error desconocido")
                }

            _isLoading.value = false
        }
    }

    fun toggleFavorite(pokemonId: Int, pokemonName: String, imageUrl: String) {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch

            _isLoading.value = true

            favoritesRepository.toggleFavorite(userId, pokemonId, pokemonName, imageUrl)
                .onSuccess { isFavorite ->
                    println("✅ Favorito cambiado: $isFavorite")
                }
                .onFailure { error ->
                    println("❌ Error toggling favorito: ${error.message}")
                    _uiState.value = FavoritesUIState.Error(error.message ?: "Error desconocido")
                }

            _isLoading.value = false
        }
    }

    suspend fun isFavorite(pokemonId: Int): Boolean {
        val userId = authRepository.currentUser?.uid ?: return false
        return favoritesRepository.isFavorite(userId, pokemonId).getOrNull() ?: false
    }

    fun refresh() {
        observeFavorites()
    }
}