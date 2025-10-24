package com.uvg.mypokedex.ui.pokedex

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.uvg.mypokedex.data.remote.dto.PokemonResult

@Composable
fun PokedexScreen(
    pokedexViewModel: PokedexViewModel = viewModel()
) {
    val uiState by pokedexViewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is PokedexUiState.Loading -> CircularProgressIndicator()
            is PokedexUiState.Error -> ErrorState(onRetry = { pokedexViewModel.loadPokemons() })
            is PokedexUiState.Success -> {
                val pokemons = (uiState as PokedexUiState.Success).pokemons
                PokemonList(
                    pokemons = pokemons,
                    onLoadMore = { pokedexViewModel.loadPokemons() }
                )
            }
        }
    }
}

@Composable
fun PokemonList(
    pokemons: List<PokemonResult>,
    onLoadMore: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(pokemons) { pokemon ->
            PokemonItem(pokemon)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLoadMore, modifier = Modifier.fillMaxWidth()) {
                Text("Cargar más")
            }
        }
    }
}

@Composable
fun PokemonItem(pokemon: PokemonResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${extractIdFromUrl(pokemon.url)}.png"
        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = pokemon.name,
            modifier = Modifier.size(80.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = pokemon.name.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium
        )
    }
}

fun extractIdFromUrl(url: String): String {
    return url.trimEnd('/').split("/").last()
}

@Composable
fun ErrorState(onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Error al cargar Pokémon")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

