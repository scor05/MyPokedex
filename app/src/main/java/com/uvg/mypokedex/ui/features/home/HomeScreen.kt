package com.uvg.mypokedex.ui.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uvg.mypokedex.ui.components.PokemonCard



@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = HomeViewModel()
) {
    val pokemonList = viewModel.getPokemonList()
    val searchQuery = rememberSaveable() { mutableStateOf("") }

    val filteredPokemonList = if (searchQuery.value.isEmpty()) {
        pokemonList
    } else {
        pokemonList.filter { it.name.contains(searchQuery.value, ignoreCase = true) }
    }

    Column (modifier = modifier.fillMaxSize()){
        TextField(
            value = searchQuery.value,
            onValueChange = { searchQuery.value = it },
            label = { Text("Buscar Pokemon") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(filteredPokemonList) { pokemon ->
            PokemonCard(pokemon)
        }
    }
}
