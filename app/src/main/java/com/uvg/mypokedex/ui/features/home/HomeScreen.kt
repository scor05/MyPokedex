package com.uvg.mypokedex.ui.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uvg.mypokedex.ui.components.PokemonCard
import com.uvg.mypokedex.ui.components.PokemonOrderButton
import androidx.compose.ui.platform.LocalContext

fun toggleOrder(currentState: Boolean, pokemonNameList: List<String>): Boolean{
    return if (currentState){
        pokemonNameList.sortedBy { it }
        false
    } else {
        pokemonNameList.sortedByDescending { it }
        true
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    isFavorite: Boolean
) {
    val pokemonList = viewModel.getPokemonList()

    val pokemonNames = pokemonList.map{it.name}

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var orderState by remember { mutableStateOf(false) }

    var filteredPokemonList = pokemonList.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    if (isFavorite){
        filteredPokemonList = filteredPokemonList.filter {viewModel.isFavorite(it.name)}
    }

    filteredPokemonList = if (orderState) {
        filteredPokemonList.sortedBy { it.name }
    } else {
        filteredPokemonList.sortedByDescending { it.name }
    }


    Column(
        modifier = modifier.fillMaxSize().padding(12.dp)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TextField(
                singleLine = true,
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar Pokemon") },
                modifier = Modifier
                    .padding(4.dp)
            )

            PokemonOrderButton(
                currentState = orderState,
                onClick = { orderState = toggleOrder(orderState, pokemonNames) }
            )
        }
        if (filteredPokemonList.isEmpty()){
            Text("No se encontraron Pokémon con los criterios de búsqueda.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredPokemonList) { pokemon ->
                    PokemonCard(
                        pokemon = pokemon,
                        isFavorite = viewModel.isFavorite(pokemon.name),
                        onToggleFavorite = { viewModel.toggleFavorite(pokemon.name); }
                    )
                }
            }
        }
        }
    }

