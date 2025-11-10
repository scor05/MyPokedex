package com.uvg.mypokedex.ui.features.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.mypokedex.data.remote.dto.ExchangeDto
import com.uvg.mypokedex.data.remote.dto.FavoritePokemonDto
import com.uvg.mypokedex.data.repository.AuthRepository
import com.uvg.mypokedex.data.repository.ExchangeRepository
import com.uvg.mypokedex.data.repository.FavoritesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExchangeViewModel(
    private val exchangeRepository: ExchangeRepository = ExchangeRepository(),
    private val favoritesRepository: FavoritesRepository = FavoritesRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExchangeUIState>(ExchangeUIState.Idle)
    val uiState: StateFlow<ExchangeUIState> = _uiState.asStateFlow()

    private val _incomingExchanges = MutableStateFlow<IncomingExchangeState>(IncomingExchangeState.NoExchanges)
    val incomingExchanges: StateFlow<IncomingExchangeState> = _incomingExchanges.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var timeoutJob: Job? = null

    init {
        observeIncomingExchanges()
    }

    // Observar propuestas de intercambio entrantes
    private fun observeIncomingExchanges() {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch

            exchangeRepository.observePendingExchanges(userId).collect { exchanges ->
                _incomingExchanges.value = if (exchanges.isEmpty()) {
                    IncomingExchangeState.NoExchanges
                } else {
                    IncomingExchangeState.HasPendingExchanges(exchanges)
                }
            }
        }
    }

    // PASO 1: Cargar mis favoritos para seleccionar
    fun startExchange() {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid
            if (userId == null) {
                _uiState.value = ExchangeUIState.Error("Usuario no autenticado")
                return@launch
            }

            _isLoading.value = true

            favoritesRepository.getFavorites(userId)
                .onSuccess { favorites ->
                    if (favorites.isEmpty()) {
                        _uiState.value = ExchangeUIState.Error("No tienes Pokémon favoritos para intercambiar")
                    } else {
                        _uiState.value = ExchangeUIState.SelectingPokemon(favorites)
                    }
                }
                .onFailure { error ->
                    _uiState.value = ExchangeUIState.Error(error.message ?: "Error cargando favoritos")
                }

            _isLoading.value = false
        }
    }

    // PASO 2: Seleccionar mi Pokémon
    fun selectMyPokemon(pokemon: FavoritePokemonDto) {
        val currentState = _uiState.value
        if (currentState is ExchangeUIState.SelectingPokemon) {
            _uiState.value = currentState.copy(selectedMyPokemon = pokemon)
        }
    }

    // PASO 3: Confirmar mi Pokémon y pasar a buscar usuario
    fun confirmMyPokemon() {
        val currentState = _uiState.value
        if (currentState is ExchangeUIState.SelectingPokemon && currentState.selectedMyPokemon != null) {
            _uiState.value = ExchangeUIState.SearchingUser(currentState.selectedMyPokemon)
        }
    }

    // PASO 4: Buscar usuario por ID o nombre
    fun searchUser(searchQuery: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is ExchangeUIState.SearchingUser) return@launch

            val myUserId = authRepository.currentUser?.uid ?: return@launch

            _isLoading.value = true

            exchangeRepository.findUser(searchQuery)
                .onSuccess { users ->
                    if (users.isEmpty()) {
                        _uiState.value = ExchangeUIState.Error("Usuario no encontrado: $searchQuery")
                    } else {
                        val (userId, userName) = users.first()

                        // No permitir intercambio consigo mismo
                        if (userId == myUserId) {
                            _uiState.value = ExchangeUIState.Error("No puedes intercambiar contigo mismo")
                            return@onSuccess
                        }

                        // Cargar favoritos del usuario objetivo
                        loadTargetUserFavorites(currentState.myPokemon, userId, userName)
                    }
                }
                .onFailure { error ->
                    _uiState.value = ExchangeUIState.Error(error.message ?: "Error buscando usuario")
                }

            _isLoading.value = false
        }
    }

    // PASO 5: Cargar favoritos del usuario objetivo
    private fun loadTargetUserFavorites(myPokemon: FavoritePokemonDto, targetUserId: String, targetUserName: String) {
        viewModelScope.launch {
            _isLoading.value = true

            favoritesRepository.getFavorites(targetUserId)
                .onSuccess { favorites ->
                    if (favorites.isEmpty()) {
                        _uiState.value = ExchangeUIState.Error("El usuario $targetUserName no tiene favoritos")
                    } else {
                        _uiState.value = ExchangeUIState.SelectingTargetPokemon(
                            myPokemon = myPokemon,
                            targetUserId = targetUserId,
                            targetUserName = targetUserName,
                            targetFavorites = favorites
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = ExchangeUIState.Error(error.message ?: "Error cargando favoritos del usuario")
                }

            _isLoading.value = false
        }
    }

    // PASO 6: Seleccionar Pokémon del usuario objetivo
    fun selectTargetPokemon(pokemon: FavoritePokemonDto) {
        val currentState = _uiState.value
        if (currentState is ExchangeUIState.SelectingTargetPokemon) {
            _uiState.value = currentState.copy(selectedTargetPokemon = pokemon)
        }
    }

    // PASO 7: Confirmar Pokémon objetivo
    fun confirmTargetPokemon() {
        val currentState = _uiState.value
        if (currentState is ExchangeUIState.SelectingTargetPokemon && currentState.selectedTargetPokemon != null) {
            _uiState.value = ExchangeUIState.ConfirmingExchange(
                myPokemon = currentState.myPokemon,
                targetUserId = currentState.targetUserId,
                targetUserName = currentState.targetUserName,
                targetPokemon = currentState.selectedTargetPokemon
            )
        }
    }

    // PASO 8: Crear propuesta de intercambio
    fun createExchangeProposal() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is ExchangeUIState.ConfirmingExchange) return@launch

            val myUserId = authRepository.currentUser?.uid ?: return@launch
            val myUserData = authRepository.getUserData(myUserId).getOrNull() ?: return@launch

            _isLoading.value = true

            exchangeRepository.createExchangeProposal(
                userAId = myUserId,
                userAName = myUserData.displayName,
                userBId = currentState.targetUserId,
                userBName = currentState.targetUserName,
                pokemonAId = currentState.myPokemon.pokemonId,
                pokemonAName = currentState.myPokemon.pokemonName,
                pokemonBId = currentState.targetPokemon.pokemonId,
                pokemonBName = currentState.targetPokemon.pokemonName
            )
                .onSuccess { exchange ->
                    _uiState.value = ExchangeUIState.WaitingForAcceptance(exchange)
                    startTimeoutCountdown(exchange.exchangeId)
                    observeExchangeStatus(exchange.exchangeId)
                }
                .onFailure { error ->
                    _uiState.value = ExchangeUIState.Error(error.message ?: "Error creando intercambio")
                }

            _isLoading.value = false
        }
    }

    // Observar cambios en el estado del intercambio
    private fun observeExchangeStatus(exchangeId: String) {
        viewModelScope.launch {
            exchangeRepository.observeExchange(exchangeId).collect { exchange ->
                if (exchange != null && exchange.status == com.uvg.mypokedex.data.remote.dto.ExchangeStatus.COMPLETED) {
                    timeoutJob?.cancel()
                    _uiState.value = ExchangeUIState.ExchangeCompleted(exchange)
                }
            }
        }
    }

    // Timeout de 90 segundos
    private fun startTimeoutCountdown(exchangeId: String) {
        timeoutJob?.cancel()
        timeoutJob = viewModelScope.launch {
            delay(90_000) // 90 segundos
            exchangeRepository.cancelExchange(exchangeId)
            _uiState.value = ExchangeUIState.Error("Tiempo de espera agotado (90s)")
        }
    }

    // ACEPTAR INTERCAMBIO (Usuario B)
    fun acceptExchange(exchange: ExchangeDto) {
        viewModelScope.launch {
            _isLoading.value = true

            exchangeRepository.acceptExchange(exchange.exchangeId)
                .onSuccess {
                    _uiState.value = ExchangeUIState.ExchangeCompleted(exchange)
                }
                .onFailure { error ->
                    _uiState.value = ExchangeUIState.Error(error.message ?: "Error aceptando intercambio")
                }

            _isLoading.value = false
        }
    }

    // RECHAZAR INTERCAMBIO
    fun rejectExchange(exchangeId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            exchangeRepository.rejectExchange(exchangeId)
                .onSuccess {
                    // Refrescar lista de pendientes
                }
                .onFailure { error ->
                    _uiState.value = ExchangeUIState.Error(error.message ?: "Error rechazando intercambio")
                }

            _isLoading.value = false
        }
    }

    // Cancelar intercambio actual
    fun cancelCurrentExchange() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ExchangeUIState.WaitingForAcceptance) {
                exchangeRepository.cancelExchange(currentState.exchange.exchangeId)
            }
            reset()
        }
    }

    // Reiniciar flujo
    fun reset() {
        timeoutJob?.cancel()
        _uiState.value = ExchangeUIState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        timeoutJob?.cancel()
    }
}