package com.uvg.mypokedex.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.uvg.mypokedex.data.Pokemon
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uvg.mypokedex.data.PokemonStat

@Composable
fun DetailScreen(pokemon: Pokemon) {
    Scaffold(
        topBar = { TopBar(pokemon.name) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            PokemonMeasurements(pokemon.height, pokemon.weight)

            Spacer(modifier = Modifier.height(16.dp))

            pokemon.stats.forEach {
                    stat ->
                PokemonStat(name = stat.name, value = stat.value)
            }
        }
    }
}