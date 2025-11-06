package com.uvg.mypokedex.data.remote.firebase

import com.google.firebase.auth.FirebaseUser
import com.uvg.mypokedex.data.remote.dto.UserDto
import com.uvg.mypokedex.data.remote.firebase.FirebaseAuthService
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val authService: FirebaseAuthService = FirebaseAuthService()
) {
    // Observar estado de autenticación
    val authStateFlow: Flow<FirebaseUser?> = authService.authStateFlow

    // Usuario actual
    val currentUser: FirebaseUser?
        get() = authService.currentUser

    // Verificar si está autenticado
    fun isAuthenticated(): Boolean = authService.isAuthenticated()

    // Login anónimo con alias
    suspend fun signInWithAlias(alias: String): Result<FirebaseUser> {
        if (alias.isBlank()) {
            return Result.failure(Exception("El alias no puede estar vacío"))
        }
        if (alias.length < 3) {
            return Result.failure(Exception("El alias debe tener al menos 3 caracteres"))
        }
        return authService.signInAnonymouslyWithAlias(alias.trim())
    }

    // Login anónimo simple
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return authService.signInAnonymously()
    }

    // Login con email
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Email y contraseña son requeridos"))
        }
        return authService.signInWithEmail(email, password)
    }

    // Registro con email
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<FirebaseUser> {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            return Result.failure(Exception("Todos los campos son requeridos"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }
        return authService.signUpWithEmail(email, password, displayName)
    }

    // Cerrar sesión
    suspend fun signOut(): Result<Unit> {
        return authService.signOut()
    }

    // Obtener datos del usuario
    suspend fun getUserData(uid: String): Result<UserDto> {
        return authService.getUserData(uid)
    }

    // Actualizar nombre
    suspend fun updateDisplayName(uid: String, newName: String): Result<Unit> {
        return authService.updateDisplayName(uid, newName)
    }
}