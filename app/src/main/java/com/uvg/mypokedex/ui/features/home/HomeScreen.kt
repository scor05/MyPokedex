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
import androidx.navigation.NavController
import com.uvg.mypokedex.navigation.AppScreens
import com.uvg.mypokedex.navigation.AppScreens.DetailScreen.createRoute
import com.uvg.mypokedex.ui.components.FavoriteButton
import com.uvg.mypokedex.ui.detail.TopBar
import com.uvg.mypokedex.ui.search.SortOption

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
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    isFavorite: Boolean
) {
    var showDialog by remember { mutableStateOf(false) }

    val pokemonList = viewModel.getVisiblePokemons()

    var searchQuery by rememberSaveable { mutableStateOf("") }

    val gridState = rememberLazyGridState()
    var requesting by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        if (pokemonList.isEmpty()) {
            viewModel.loadMorePokemon()
        }
    }

    var filteredPokemonList = pokemonList.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    if (isFavorite) {
        filteredPokemonList = filteredPokemonList.filter { viewModel.isFavorite(it.name) }
    }

    filteredPokemonList = when (viewModel.sortOption) {
        SortOption.Numero -> if (viewModel.ascending) {
            filteredPokemonList.sortedBy { it.id }
        } else {
            filteredPokemonList.sortedByDescending { it.id }
        }
        SortOption.Nombre -> if (viewModel.ascending) {
            filteredPokemonList.sortedBy { it.name }
        } else {
            filteredPokemonList.sortedByDescending { it.name }
        }
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
            TopBar(
                navController = navController,
                title = "MyPokedex",
                homeViewModel = viewModel
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    item (span = {GridItemSpan(maxLineSpan)}) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            TextField(
                                singleLine = true,
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Buscar Pokémon") },
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                    items(
                        items = filteredPokemonList,
                        key = { it.id }
                    ) { pokemon ->
                        PokemonCard(
                            pokemon = pokemon,
                            isFavorite = viewModel.isFavorite(pokemon.name),
                            onToggleFavorite = { viewModel.toggleFavorite(pokemon.name) },
                            onItemClick = {navController.navigate(createRoute(pokemon.name))}
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        SearchToolsDialog(
            selected = viewModel.sortOption,
            ascending = viewModel.ascending,
            favorites = viewModel.favoritesToggle,
            onFavoriteChange = { viewModel.setFavoritesOnly(it) },
            onSelectedChange = { viewModel.setSortOptionCustom(it) },
            onAscendingChange = { viewModel.setAscendingCustom(it) },
            onDismiss = { showDialog = false }
        )
    }
}
