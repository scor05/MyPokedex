package com.uvg.mypokedex.ui.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.mypokedex.data.remote.firebase.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUIState>(AuthUIState.NotAuthenticated)
    val uiState: StateFlow<AuthUIState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            authRepository.authStateFlow.collect { user ->
                _uiState.value = if (user != null) {
                    AuthUIState.Authenticated(user)
                } else {
                    AuthUIState.NotAuthenticated
                }
            }
        }
    }

    // Login con Alias
    fun signInWithAlias(alias: String) {
        viewModelScope.launch {
            _formState.value = _formState.value.copy(isLoading = true, errorMessage = null)

            authRepository.signInWithAlias(alias)
                .onSuccess { user ->
                    _uiState.value = AuthUIState.Authenticated(user)
                    _formState.value = AuthFormState() // Reset form
                }
                .onFailure { error ->
                    _formState.value = _formState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al iniciar sesión"
                    )
                }
        }
    }

    // Login anónimo simple
    fun signInAnonymously() {
        viewModelScope.launch {
            _formState.value = _formState.value.copy(isLoading = true, errorMessage = null)

            authRepository.signInAnonymously()
                .onSuccess { user ->
                    _uiState.value = AuthUIState.Authenticated(user)
                    _formState.value = AuthFormState()
                }
                .onFailure { error ->
                    _formState.value = _formState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al iniciar sesión"
                    )
                }
        }
    }

    // Login con Email
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _formState.value = _formState.value.copy(isLoading = true, errorMessage = null)

            authRepository.signInWithEmail(email, password)
                .onSuccess { user ->
                    _uiState.value = AuthUIState.Authenticated(user)
                    _formState.value = AuthFormState()
                }
                .onFailure { error ->
                    _formState.value = _formState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al iniciar sesión"
                    )
                }
        }
    }

    // Registro con Email
    fun signUpWithEmail(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _formState.value = _formState.value.copy(isLoading = true, errorMessage = null)

            authRepository.signUpWithEmail(email, password, displayName)
                .onSuccess { user ->
                    _uiState.value = AuthUIState.Authenticated(user)
                    _formState.value = AuthFormState()
                }
                .onFailure { error ->
                    _formState.value = _formState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al crear cuenta"
                    )
                }
        }
    }

    // Cerrar sesión
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = AuthUIState.NotAuthenticated
        }
    }

    // Actualizar campos del formulario
    fun updateAlias(alias: String) {
        _formState.value = _formState.value.copy(alias = alias)
    }

    fun updateEmail(email: String) {
        _formState.value = _formState.value.copy(email = email)
    }

    fun updatePassword(password: String) {
        _formState.value = _formState.value.copy(password = password)
    }

    fun updateDisplayName(displayName: String) {
        _formState.value = _formState.value.copy(displayName = displayName)
    }

    fun clearError() {
        _formState.value = _formState.value.copy(errorMessage = null)
    }

    // Verificar si está autenticado
    fun isAuthenticated(): Boolean = authRepository.isAuthenticated()
}