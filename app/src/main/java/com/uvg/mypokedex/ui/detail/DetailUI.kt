package com.uvg.mypokedex.ui.detail

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.util.Locale

@Composable
fun DetailUI(
    pokemonName: String,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as Application
    val viewModel: PokemonDetailViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(app)
    )

    val uiState by viewModel.uiState.collectAsState(initial = DetailUiState.Loading)
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
        when (val state = uiState) {
            is DetailUiState.Loading -> CircularProgressIndicator()
            is DetailUiState.Error -> Text("Error al cargar los datos.")
            is DetailUiState.Success -> {
                val pokemon = state.pokemon

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
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = pokemon.name.replaceFirstChar { it.uppercaseChar() },
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 40.sp
                        )

                        AsyncImage(
                            model = pokemon.sprites.front_default,
                            contentDescription = "Imagen de ${pokemon.name}",
                            modifier = Modifier
                                .size(220.dp)
                                .padding(top = 10.dp)
                        )

                        // ID
                        Text(
                            text = "#${String.format(Locale.getDefault(), "%03d", pokemon.id)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(top = 8.dp))

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

                        HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(top = 8.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Estadísticas base:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )

                            StatRowDecimal(
                                name = "Altura",
                                value = pokemon.height / 10.0,
                                unit = "m",
                                maxValue = 30.0
                            )

                            StatRowDecimal(
                                name = "Peso",
                                value = pokemon.weight / 10.0,
                                unit = "kg",
                                maxValue = 300.0
                            )

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