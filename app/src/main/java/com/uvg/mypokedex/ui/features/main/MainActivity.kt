package com.uvg.mypokedex.ui.features.main

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uvg.mypokedex.ui.components.PokemonOrderButton
import com.uvg.mypokedex.ui.detail.DetailUI
import com.uvg.mypokedex.ui.detail.TopBar
import com.uvg.mypokedex.ui.features.home.HomeScreen
import com.uvg.mypokedex.ui.features.home.HomeViewModel
import com.uvg.mypokedex.ui.theme.MyPokedexTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyPokedexTheme {
                var isFavoriteState by remember { mutableStateOf(false) }
                val homeViewModel = HomeViewModel()
                Scaffold(
                    modifier = Modifier.fillMaxWidth(),
                    topBar = {
                        TopBar(
                            pokemonName = "MI POKEDEX",
                            showFavorite = true,
                            isFavorite = isFavoriteState,
                            onToggleFavorite = {isFavoriteState = !isFavoriteState}
                        )
                    }
                ) { innerPadding ->
                    HomeScreen(modifier = Modifier.padding(vertical = 50.dp, horizontal = 8.dp),
                        viewModel = homeViewModel )
                }
            }
        }
    }
}

//@Composable
//fun AppNavigation(homeViewModel: HomeViewModel) {
//    // Muestra Pikachu como ejemplo
//    DetailUI(
//        pokemonName = "Pikachu",
//        isFavorite = homeViewModel.isFavorite("Pikachu"),
//        onToggleFavorite = { homeViewModel.toggleFavorite("Pikachu") }
//    )
//}