package com.uvg.mypokedex.ui.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.uvg.mypokedex.navigation.AppScreens
import com.uvg.mypokedex.navigation.AppScreens.DetailScreen.createRoute
import com.uvg.mypokedex.ui.components.PokemonCard
import com.uvg.mypokedex.ui.detail.TopBar
import com.uvg.mypokedex.ui.search.SearchToolsDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pokemonList = viewModel.getVisiblePokemons()

    Scaffold(
        topBar = {
            TopBar(
                navController = navController,
                title = "MyPokedex",
                homeViewModel = viewModel
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading && uiState.pokemons.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Cargando Pokémon...")
                    }
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            uiState.error ?: "Error desconocido",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("Reintentar")
                        }
                    }
                }

                pokemonList.isEmpty() -> {
                    Box(Modifier.padding(16.dp).fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se han podido encontrar Pokémon con los criterios de búsqueda")
                    }
                }

                else -> {
                    val gridState = rememberLazyGridState()

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pokemonList, key = { it.id }) { pokemon ->
                            PokemonCard(
                                pokemon = pokemon,
                                isFavorite = viewModel.isFavorite(pokemon.name),
                                onToggleFavorite = { viewModel.toggleFavorite(pokemon.name) },
                                onItemClick = {
                                    navController.navigate(createRoute(pokemon.name))
                                }
                            )
                        }

                        if (uiState.isLoading && uiState.pokemons.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }

                    LaunchedEffect(gridState) {
                        snapshotFlow {
                            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                            val total = gridState.layoutInfo.totalItemsCount
                            lastVisible != null && lastVisible >= total - 4
                        }.collect { shouldLoadMore ->
                            if (shouldLoadMore && !uiState.isLoading && !uiState.endReached) {
                                viewModel.loadMorePokemon()
                            }
                        }
                    }
                }
            }

            if (viewModel.showDialog) {
                SearchToolsDialog(
                    selected = viewModel.sortOption,
                    ascending = viewModel.ascending,
                    favorites = viewModel.favoritesToggle,
                    onSelectedChange = { viewModel.setSortOptionCustom(it) },
                    onAscendingChange = { viewModel.setAscendingCustom(it) },
                    onFavoriteChange = { viewModel.setFavoritesOnly(it) },
                    onDismiss = { viewModel.toggleDialog(false) }
                )
            }
        }
    }
}
