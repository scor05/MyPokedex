package com.uvg.mypokedex.ui.features.main

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.uvg.mypokedex.ui.detail.DetailUI
import com.uvg.mypokedex.ui.detail.TopBar
import com.uvg.mypokedex.ui.features.home.HomeScreen
import com.uvg.mypokedex.ui.features.home.HomeViewModel
import com.uvg.mypokedex.ui.theme.MyPokedexTheme
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyPokedexTheme {
                Scaffold(
                    topBar = { TopBar(
                        "MI POKEDEX",
                        showFavorite = TODO(),
                        isFavorite = TODO(),
                        onToggleFavorite = TODO()
                    ) } // 👈 aquí va el topBar
                ) { innerPadding ->
                    HomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppNavigation(homeViewModel: HomeViewModel = viewModel()) {
    // Muestra Pikachu como ejemplo
    DetailUI(
        pokemonName = "Pikachu",
        isFavorite = homeViewModel.isFavorite("Pikachu"),
        onToggleFavorite = { homeViewModel.toggleFavorite("Pikachu") }
    )
}