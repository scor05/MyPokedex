package com.uvg.mypokedex.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.uvg.mypokedex.data.remote.dto.UserDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthService {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Observar el estado de autenticación
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // Usuario actual
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    // Verificar si está autenticado
    fun isAuthenticated(): Boolean = currentUser != null

    // Login Anónimo
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user ?: throw Exception("Usuario nulo después de login anónimo")

            // Crear documento de usuario en Firestore
            createUserDocument(user.uid, "Usuario Anónimo")

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login con Email y Contraseña
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Usuario nulo después de login")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Registro con Email y Contraseña
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Usuario nulo después de registro")

            // Crear documento de usuario en Firestore
            createUserDocument(user.uid, displayName, email)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login Anónimo con Alias Personalizado (RECOMENDADO PARA LA COMPETENCIA)
    suspend fun signInAnonymouslyWithAlias(alias: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user ?: throw Exception("Usuario nulo después de login anónimo")

            // Crear documento con alias personalizado
            createUserDocument(user.uid, alias)

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cerrar sesión
    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Crear documento de usuario en Firestore
    private suspend fun createUserDocument(uid: String, displayName: String, email: String? = null) {
        try {
            val userDto = UserDto(
                uid = uid,
                displayName = displayName,
                email = email,
                createdAt = System.currentTimeMillis(),
                favoritePokemonIds = emptyList()
            )

            firestore.collection("users")
                .document(uid)
                .set(userDto)
                .await()
        } catch (e: Exception) {
            // Log error pero no fallar el login
            println("Error creando documento de usuario: ${e.message}")
        }
    }

    // Obtener datos del usuario desde Firestore
    suspend fun getUserData(uid: String): Result<UserDto> {
        return try {
            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val userDto = document.toObject(UserDto::class.java)
                ?: throw Exception("Usuario no encontrado en Firestore")

            Result.success(userDto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar nombre de usuario
    suspend fun updateDisplayName(uid: String, newName: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(uid)
                .update("displayName", newName)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}