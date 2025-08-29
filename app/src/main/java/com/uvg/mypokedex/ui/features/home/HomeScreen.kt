package com.uvg.mypokedex.ui.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uvg.mypokedex.ui.components.PokemonCard
import com.uvg.mypokedex.ui.components.UnstablePokemonList
import kotlin.random.Random


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = HomeViewModel()
) {
    val pokemonList = viewModel.getPokemonList()
    // Remember en la lista para que sea inmutable
    val pokemonNames = remember (pokemonList) { pokemonList.map { it.name } }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Sacar el UnstablePokemonList del bucle
        item {
            UnstablePokemonList(pokemonNames)
        }
        items(pokemonList) { pokemon ->
            PokemonCard(pokemon)
        }
    }
}
