package com.uvg.mypokedex.ui.features.exchange

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExchangeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intercambios") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.SwapHoriz, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentUser != null) {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.SwapHoriz, "Nuevo intercambio")
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
                is ExchangeUIState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ExchangeUIState.Error -> {
                    ErrorMessage(
                        message = state.message,
                        onRetry = { viewModel.refresh() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ExchangeUIState.Empty -> {
                    EmptyMessage(modifier = Modifier.align(Alignment.Center))
                }
                is ExchangeUIState.Success -> {
                    ExchangeList(
                        exchanges = state.exchanges,
                        onAccept = { viewModel.acceptExchange(it) },
                        onReject = { viewModel.rejectExchange(it) }
                    )
                }
            }
        }
    }

    if (showCreateDialog && currentUser != null) {
        CreateExchangeDialog(
            currentUserId = currentUser!!.uid,
            currentUserName = currentUser!!.displayName ?: "Usuario",
            onDismiss = { showCreateDialog = false },
            onConfirm = { userBId, userBName, pokemonAId, pokemonAName, pokemonBId, pokemonBName ->
                viewModel.createExchange(
                    currentUser!!.uid,
                    currentUser!!.displayName ?: "Usuario",
                    userBId,
                    userBName,
                    pokemonAId,
                    pokemonAName,
                    pokemonBId,
                    pokemonBName
                )
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun ExchangeList(
    exchanges: List<com.uvg.mypokedex.data.remote.dto.ExchangeDto>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(exchanges, key = { it.exchangeId }) { exchange ->
            ExchangeCard(
                exchange = exchange,
                onAccept = { onAccept(exchange.exchangeId) },
                onReject = { onReject(exchange.exchangeId) }
            )
        }
    }
}

@Composable
private fun ExchangeCard(
    exchange: com.uvg.mypokedex.data.remote.dto.ExchangeDto,
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
                text = "Intercambio de ${exchange.userAName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PokemonExchangeItem(
                    pokemonId = exchange.pokemonAId,
                    pokemonName = exchange.pokemonAName,
                    label = "Ofrece"
                )

                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Intercambio",
                    modifier = Modifier.size(32.dp)
                )

                PokemonExchangeItem(
                    pokemonId = exchange.pokemonBId,
                    pokemonName = exchange.pokemonBName,
                    label = "Por"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Rechazar")
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aceptar")
                }
            }
        }
    }
}

@Composable
private fun PokemonExchangeItem(
    pokemonId: Int,
    pokemonName: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
        AsyncImage(
            model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png",
            contentDescription = pokemonName,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = pokemonName.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CreateExchangeDialog(
    currentUserId: String,
    currentUserName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String, Int, String) -> Unit
) {
    var userBId by remember { mutableStateOf("") }
    var userBName by remember { mutableStateOf("") }
    var pokemonAId by remember { mutableStateOf("") }
    var pokemonAName by remember { mutableStateOf("") }
    var pokemonBId by remember { mutableStateOf("") }
    var pokemonBName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Intercambio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = userBId,
                    onValueChange = { userBId = it },
                    label = { Text("ID del otro usuario") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = userBName,
                    onValueChange = { userBName = it },
                    label = { Text("Nombre del otro usuario") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = pokemonAId,
                    onValueChange = { pokemonAId = it },
                    label = { Text("ID de tu Pokémon") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = pokemonAName,
                    onValueChange = { pokemonAName = it },
                    label = { Text("Nombre de tu Pokémon") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = pokemonBId,
                    onValueChange = { pokemonBId = it },
                    label = { Text("ID del Pokémon a recibir") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = pokemonBName,
                    onValueChange = { pokemonBName = it },
                    label = { Text("Nombre del Pokémon a recibir") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val pokAId = pokemonAId.toIntOrNull() ?: 0
                    val pokBId = pokemonBId.toIntOrNull() ?: 0
                    if (userBId.isNotBlank() && pokAId > 0 && pokBId > 0) {
                        onConfirm(userBId, userBName, pokAId, pokemonAName, pokBId, pokemonBName)
                    }
                }
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun EmptyMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No tienes intercambios pendientes", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}