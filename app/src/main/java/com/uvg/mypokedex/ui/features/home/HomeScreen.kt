package com.uvg.mypokedex.ui.features.home



import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uvg.mypokedex.ui.components.PokemonCard

@Composable
fun HomeScreen(viewModel: HomeViewModel = HomeViewModel()){
    val pokemonList = viewModel.getPokemonList()

    LazyVerticalGrid (
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        columns = GridCells.Fixed(2)
    ) {
        items(pokemonList) {
                pokemon ->
            PokemonCard(pokemon)
        }
    }
}
