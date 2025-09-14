package com.uvg.mypokedex.ui.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uvg.mypokedex.ui.components.PokemonCard
import com.uvg.mypokedex.ui.components.PokemonOrderButton
import com.uvg.mypokedex.ui.search.SearchToolsDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import androidx.compose.material3.*
import androidx.compose.runtime.*

fun toggleOrder(currentState: Boolean, pokemonNameList: List<String>): Boolean {
    return if (currentState) {
        pokemonNameList.sortedBy { it }
        false
    } else {
        pokemonNameList.sortedByDescending { it }
        true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    isFavorite: Boolean
) {
    var showDialog by remember { mutableStateOf(false) }

    val pokemonList = viewModel.pokemons
    val pokemonNames = pokemonList.map { it.name }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var orderState by remember { mutableStateOf(false) }

    val gridState = rememberLazyGridState()
    var requesting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (viewModel.pokemons.isEmpty()) {
            viewModel.loadMorePokemon()
        }
    }


    var filteredPokemonList = pokemonList.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }
    if (isFavorite) {
        filteredPokemonList = filteredPokemonList.filter { viewModel.isFavorite(it.name) }
    }
    filteredPokemonList = if (orderState) {
        filteredPokemonList.sortedBy { it.name }
    } else {
        filteredPokemonList.sortedByDescending { it.name }
    }


    LaunchedEffect(gridState, filteredPokemonList.size) {
        snapshotFlow {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = gridState.layoutInfo.totalItemsCount
            lastVisible to total
        }
            .map { (lastVisible, total) ->
                val buffer = 4
                lastVisible >= total - 1 - buffer
            }
            .distinctUntilChanged()
            .collectLatest { shouldLoad ->
                if (shouldLoad && !requesting) {
                    requesting = true
                    try {
                        viewModel.loadMorePokemon()
                    } finally {
                        requesting = false
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyPokedex") },
                actions = {

                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Herramientas de búsqueda"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp)
        ) {
            // Barra de búsqueda
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextField(
                    singleLine = true,
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar Pokémon") },
                    modifier = Modifier.padding(4.dp)
                )

                PokemonOrderButton(
                    currentState = orderState,
                    onClick = { orderState = toggleOrder(orderState, pokemonNames) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))


            if (filteredPokemonList.isEmpty()) {
                Text("No se encontraron Pokémon con los criterios de búsqueda.")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = filteredPokemonList,
                        key = { it.id }
                    ) { pokemon ->
                        PokemonCard(
                            pokemon = pokemon,
                            isFavorite = viewModel.isFavorite(pokemon.name),
                            onToggleFavorite = { viewModel.toggleFavorite(pokemon.name) }
                        )
                    }
                }
            }
        }
    }

    // ---- Diálogo de herramientas de búsqueda ----
    if (showDialog) {
        SearchToolsDialog(
            onDismiss = { showDialog = false }
        )
    }
}
