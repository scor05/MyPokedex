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
import com.google.firebase.FirebaseApp
import com.uvg.mypokedex.navigation.AppNavigationHost
import com.uvg.mypokedex.ui.features.auth.AuthUIState
import com.uvg.mypokedex.ui.features.home.HomeViewModel
import com.uvg.mypokedex.ui.theme.MyPokedexTheme
import com.uvg.mypokedex.ui.features.auth.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)

        setContent {
            MyPokedexTheme {
                val navController = rememberNavController()
                val homeViewModel: HomeViewModel = viewModel(
                    factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
                )
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.uiState.collectAsState()
                var isFavoriteState by remember { mutableStateOf(false) }

                AppNavigationHost(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    favoriteToggled = isFavoriteState
                )

                val activity = LocalActivity.current
                BackHandler {
                    val popped = navController.popBackStack()
                    if (!popped) {
                        activity?.moveTaskToBack(true)
                    }
                }
            }
        }
    }
}