package com.uvg.mypokedex.ui.features.auth

import com.google.firebase.auth.FirebaseUser

sealed interface AuthUIState {
    data object Loading : AuthUIState
    data object NotAuthenticated : AuthUIState
    data class Authenticated(val user: FirebaseUser) : AuthUIState
    data class Error(val message: String) : AuthUIState
}

data class AuthFormState(
    val alias: String = "",
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)