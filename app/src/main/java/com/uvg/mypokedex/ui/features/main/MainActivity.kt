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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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