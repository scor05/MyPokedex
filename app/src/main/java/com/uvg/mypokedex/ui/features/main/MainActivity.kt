package com.uvg.mypokedex.ui.features.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.uvg.mypokedex.navigation.AppNavigationHost
import com.uvg.mypokedex.ui.features.home.HomeViewModel
import com.uvg.mypokedex.ui.theme.MyPokedexTheme
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.uvg.mypokedex.ui.components.AuthDialog
import com.uvg.mypokedex.ui.features.auth.AuthUIState
import com.uvg.mypokedex.ui.features.auth.AuthViewModel

class MainActivity : ComponentActivity() {
    private val TAG = "FirebaseTest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)

        // Probar conexión con Firestore
        val db = FirebaseFirestore.getInstance()
        val testData = hashMapOf(
            "mensaje" to "Hola Firebase!",
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("prueba_sin_ui")
            .add(testData)
            .addOnSuccessListener { documentRef ->
                Log.d(TAG, "✅ Documento creado con ID: ${documentRef.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al escribir en Firestore", e)
            }

        setContent {
            val navController = rememberNavController()
            val homeViewModel: HomeViewModel = viewModel(
                factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            )
            var isFavoriteState by remember { mutableStateOf(false) }

            MyPokedexTheme {
                AppNavigationHost(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    favoriteToggled = isFavoriteState
                )

                val activity = LocalActivity.current
                BackHandler {
                    val popped = navController.popBackStack()
                    if (!popped) {
                        // En la raíz: NO finish(); envía la tarea al background
                        activity?.moveTaskToBack(true)
                    }
                }
            }
        }
    }
}

@Composable
fun TestAuthScreen() {
    val authViewModel: AuthViewModel = viewModel()
    val uiState by authViewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (uiState) {
            is AuthUIState.NotAuthenticated -> {
                Text("No autenticado")
                Button(onClick = { showDialog = true }) {
                    Text("Iniciar Sesión")
                }
            }
            is AuthUIState.Authenticated -> {
                val user = (uiState as AuthUIState.Authenticated).user
                Text("✅ Autenticado como: ${user.uid}")
                Button(onClick = { authViewModel.signOut() }) {
                    Text("Cerrar Sesión")
                }
            }
            else -> CircularProgressIndicator()
        }
    }

    if (showDialog) {
        AuthDialog(
            onDismiss = { showDialog = false },
            onAuthSuccess = { showDialog = false }
        )
    }
}