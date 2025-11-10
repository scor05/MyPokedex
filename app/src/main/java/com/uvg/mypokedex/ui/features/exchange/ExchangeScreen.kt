package com.uvg.mypokedex.ui.features.exchangepackage


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.uvg.mypokedex.data.remote.dto.ExchangeDto
import com.uvg.mypokedex.data.remote.dto.FavoritePokemonDto
import com.uvg.mypokedex.ui.features.exchange.ExchangeUIState
import com.uvg.mypokedex.ui.features.exchange.ExchangeViewModel
import com.uvg.mypokedex.ui.features.exchange.IncomingExchangeState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExchangeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val incomingExchanges by viewModel.incomingExchanges.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intercambio de Pokémon") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState is ExchangeUIState.Idle) {
                FloatingActionButton(onClick = { viewModel.startExchange() }) {
                    Icon(Icons.Default.Add, "Nuevo intercambio")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is ExchangeUIState.Idle -> {
                    IdleContent(
                        incomingState = incomingExchanges,
                        onAcceptExchange = { viewModel.acceptExchange(it) },
                        onRejectExchange = { viewModel.rejectExchange(it.exchangeId) }
                    )
                }
                is ExchangeUIState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ExchangeUIState.SelectingPokemon -> {
                    SelectMyPokemonScreen(
                        favorites = state.myFavorites,
                        selected = state.selectedMyPokemon,
                        onSelect = { viewModel.selectMyPokemon(it) },
                        onConfirm = { viewModel.confirmMyPokemon() },
                        onCancel = { viewModel.reset() }
                    )
                }
                is ExchangeUIState.SearchingUser -> {
                    SearchUserScreen(
                        myPokemon = state.myPokemon,
                        onSearch = { viewModel.searchUser(it) },
                        onCancel = { viewModel.reset() }
                    )
                }
                is ExchangeUIState.SelectingTargetPokemon -> {
                    SelectTargetPokemonScreen(
                        targetUserName = state.targetUserName,
                        favorites = state.targetFavorites,
                        selected = state.selectedTargetPokemon,
                        onSelect = { viewModel.selectTargetPokemon(it) },
                        onConfirm = { viewModel.confirmTargetPokemon() },
                        onCancel = { viewModel.reset() }
                    )
                }
                is ExchangeUIState.ConfirmingExchange -> {
                    ConfirmExchangeScreen(
                        myPokemon = state.myPokemon,
                        targetPokemon = state.targetPokemon,
                        targetUserName = state.targetUserName,
                        onConfirm = { viewModel.createExchangeProposal() },
                        onCancel = { viewModel.reset() }
                    )
                }
                is ExchangeUIState.WaitingForAcceptance -> {
                    WaitingScreen(
                        exchange = state.exchange,
                        onCancel = { viewModel.cancelCurrentExchange() }
                    )
                }
                is ExchangeUIState.ExchangeCompleted -> {
                    CompletedScreen(
                        exchange = state.exchange,
                        onDone = { viewModel.reset() }
                    )
                }
                is ExchangeUIState.Error -> {
                    ErrorScreen(
                        message = state.message,
                        onRetry = { viewModel.reset() }
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                )
            }
        }
    }
}

@Composable
private fun IdleContent(
    incomingState: IncomingExchangeState,
    onAcceptExchange: (ExchangeDto) -> Unit,
    onRejectExchange: (ExchangeDto) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Presiona + para iniciar un intercambio",
            style = MaterialTheme.typography.titleMedium
        )

        when (incomingState) {
            is IncomingExchangeState.HasPendingExchanges -> {
                Text(
                    "Propuestas recibidas:",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(incomingState.exchanges) { exchange ->
                        IncomingExchangeCard(
                            exchange = exchange,
                            onAccept = { onAcceptExchange(exchange) },
                            onReject = { onRejectExchange(exchange) }
                        )
                    }
                }
            }
            IncomingExchangeState.NoExchanges -> {
                Text("No tienes propuestas pendientes")
            }
        }
    }
}

@Composable
private fun IncomingExchangeCard(
    exchange: ExchangeDto,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "${exchange.userAName} quiere intercambiar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PokemonItem(
                    pokemonId = exchange.pokemonAId,
                    pokemonName = exchange.pokemonAName,
                    label = "Ofrece"
                )
                Icon(Icons.Default.SwapHoriz, null, Modifier.size(32.dp))
                PokemonItem(
                    pokemonId = exchange.pokemonBId,
                    pokemonName = exchange.pokemonBName,
                    label = "Por tu"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) {
                    Text("Rechazar")
                }
                Button(onClick = onAccept, modifier = Modifier.weight(1f)) {
                    Text("Aceptar")
                }
            }
        }
    }
}

@Composable
private fun SelectMyPokemonScreen(
    favorites: List<FavoritePokemonDto>,
    selected: FavoritePokemonDto?,
    onSelect: (FavoritePokemonDto) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Selecciona tu Pokémon para intercambiar",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favorites) { pokemon ->
                PokemonSelectCard(
                    pokemon = pokemon,
                    isSelected = selected?.pokemonId == pokemon.pokemonId,
                    onClick = { onSelect(pokemon) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(
                onClick = onConfirm,
                enabled = selected != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Siguiente")
            }
        }
    }
}

@Composable
private fun SearchUserScreen(
    myPokemon: FavoritePokemonDto,
    onSearch: (String) -> Unit,
    onCancel: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Intercambiar:", style = MaterialTheme.typography.titleLarge)

        PokemonDisplayCard(myPokemon)

        Text("Buscar usuario:", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("ID o nombre de usuario") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(
                onClick = { onSearch(searchText) },
                enabled = searchText.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Buscar")
            }
        }
    }
}

@Composable
private fun SelectTargetPokemonScreen(
    targetUserName: String,
    favorites: List<FavoritePokemonDto>,
    selected: FavoritePokemonDto?,
    onSelect: (FavoritePokemonDto) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Pokémon de $targetUserName",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favorites) { pokemon ->
                PokemonSelectCard(
                    pokemon = pokemon,
                    isSelected = selected?.pokemonId == pokemon.pokemonId,
                    onClick = { onSelect(pokemon) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(
                onClick = onConfirm,
                enabled = selected != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Confirmar")
            }
        }
    }
}

@Composable
private fun ConfirmExchangeScreen(
    myPokemon: FavoritePokemonDto,
    targetPokemon: FavoritePokemonDto,
    targetUserName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            "Confirmar Intercambio",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Das:", style = MaterialTheme.typography.titleSmall)
                PokemonDisplayCard(myPokemon)

                Icon(
                    Icons.Default.SwapVert,
                    null,
                    Modifier.size(48.dp).padding(vertical = 8.dp)
                )

                Text("Recibes de $targetUserName:", style = MaterialTheme.typography.titleSmall)
                PokemonDisplayCard(targetPokemon)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(onClick = onConfirm, modifier = Modifier.weight(1f)) {
                Text("Enviar Propuesta")
            }
        }
    }
}

@Composable
private fun WaitingScreen(
    exchange: ExchangeDto,
    onCancel: () -> Unit
) {
    var timeLeft by remember { mutableStateOf(90) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp))

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Esperando respuesta de ${exchange.userBName}...",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            "Tiempo restante: ${timeLeft}s",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(onClick = onCancel) {
            Text("Cancelar")
        }
    }
}

@Composable
private fun CompletedScreen(
    exchange: ExchangeDto,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            null,
            Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "¡Intercambio completado!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Has intercambiado ${exchange.pokemonAName} por ${exchange.pokemonBName}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onDone) {
            Text("Volver")
        }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            null,
            Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Error",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )

        Text(message, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Text("Volver")
        }
    }
}

@Composable
private fun PokemonSelectCard(
    pokemon: FavoritePokemonDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.pokemonName,
                modifier = Modifier.size(64.dp)
            )
            Text(
                pokemon.pokemonName.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun PokemonDisplayCard(pokemon: FavoritePokemonDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.pokemonName,
                modifier = Modifier.size(120.dp)
            )
            Text(
                pokemon.pokemonName.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PokemonItem(
    pokemonId: Int,
    pokemonName: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        AsyncImage(
            model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png",
            contentDescription = pokemonName,
            modifier = Modifier.size(64.dp)
        )
        Text(
            pokemonName.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}