package com.uvg.mypokedex.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.util.Locale

@Composable
fun DetailUI(
    pokemonName: String,
    modifier: Modifier = Modifier,
    viewModel: PokemonDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(pokemonName) {
        viewModel.loadPokemon(pokemonName)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        contentAlignment = Alignment.TopCenter
    ) {
        when (uiState) {
            is DetailUiState.Loading -> CircularProgressIndicator()
            is DetailUiState.Error -> Text("Error al cargar los datos.")
            is DetailUiState.Success -> {
                val pokemon = (uiState as DetailUiState.Success).pokemon

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Nombre
                        Text(
                            text = pokemon.name.replaceFirstChar { it.uppercaseChar() },
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 40.sp
                        )

                        // Imagen
                        AsyncImage(
                            model = pokemon.sprites.front_default,
                            contentDescription = "Imagen de ${pokemon.name}",
                            modifier = Modifier.size(160.dp)
                        )

                        Divider(thickness = 1.dp)

                        // Tipos
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Tipos:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            val tipos = pokemon.types.joinToString(", ") { slot ->
                                slot.type.name.replaceFirstChar { it.uppercaseChar() }
                            }
                            Text(
                                text = tipos,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Divider(thickness = 1.dp)

                        // Estadísticas base
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Estadísticas base:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )

                            // Altura con decimal
                            StatRowDecimal(
                                name = "Altura",
                                value = pokemon.height / 10.0,
                                unit = "m",
                                maxValue = 30.0
                            )

                            // Peso con decimal
                            StatRowDecimal(
                                name = "Peso",
                                value = pokemon.weight / 10.0,
                                unit = "kg",
                                maxValue = 300.0
                            )

                            // Stats del API
                            pokemon.stats.forEach { stat ->
                                StatRow(
                                    name = stat.stat.name,
                                    value = stat.base_stat
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(name: String, value: Int, unit: String? = null, maxValue: Int = 200) {
    val formattedName = when (name.lowercase(Locale.ROOT)) {
        "hp" -> "HP"
        "attack" -> "Attack"
        "defense" -> "Defense"
        "special-attack" -> "Sp. Atk"
        "special-defense" -> "Sp. Def"
        "speed" -> "Speed"
        else -> name.replaceFirstChar { it.uppercaseChar() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (unit != null) "$formattedName: $value $unit" else "$formattedName: $value",
            style = MaterialTheme.typography.bodyMedium
        )
        LinearProgressIndicator(
            progress = { (value / maxValue.toFloat()).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(8.dp)
                .padding(bottom = 4.dp)
        )
    }
}

@Composable
fun StatRowDecimal(name: String, value: Double, unit: String, maxValue: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = String.format("%s: %.1f %s", name, value, unit),
            style = MaterialTheme.typography.bodyMedium
        )
        LinearProgressIndicator(
            progress = { (value / maxValue).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(8.dp)
                .padding(bottom = 4.dp)
        )
    }
}
