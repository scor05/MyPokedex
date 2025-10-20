package com.uvg.mypokedex.ui.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.uvg.mypokedex.navigation.AppScreens.DetailScreen.createRoute
import com.uvg.mypokedex.ui.components.PokemonCard
import com.uvg.mypokedex.ui.detail.TopBar
import com.uvg.mypokedex.ui.search.SearchToolsDialog
import com.uvg.mypokedex.ui.search.SortOption
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    isFavorite: Boolean
) {
    var showDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val gridState = rememberLazyGridState()
    var requesting by remember { mutableStateOf(false) }

    var filteredList = viewModel.getVisiblePokemons()
        .filter { it.name.contains(searchQuery, ignoreCase = true) }

    if (isFavorite) {
        filteredList = filteredList.filter { viewModel.isFavorite(it.name) }
    }

    LaunchedEffect(gridState, filteredList.size) {
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
                if (shouldLoad && !requesting && !uiState.isLoading) {
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading && uiState.pokemons.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(uiState.error ?: "Error desconocido")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("Reintentar")
                        }
                    }
                }
                filteredList.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron Pokémon con los criterios de búsqueda.")
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextField(
                            singleLine = true,
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                            },
                            label = { Text("Buscar Pokémon") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = gridState,
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredList, key = { it.id }) { pokemon ->
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
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
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
