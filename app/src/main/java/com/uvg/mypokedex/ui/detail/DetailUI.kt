@file:OptIn(ExperimentalMaterial3Api::class)

package com.uvg.mypokedex.ui.detail

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uvg.mypokedex.data.*
import androidx.compose.foundation.layout.*


@Composable
fun FavoriteButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Text("❤")
    }
}

@Composable
fun TopBar(pokemonName: String, showFavorite: Boolean = true) {
    CenterAlignedTopAppBar(
        title = { Text(pokemonName) },
        navigationIcon = { IconButton(onClick = { }) { Text("<") } },
        actions = {
            if (showFavorite) {
                FavoriteButton { }
            }
        }
    )
}

@Composable
fun PokemonMeasurements(height: Float, weight: Float){
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly){
        Text(text = "Height: $height m")
        Text(text = "Weight: $weight kg")
    }
}


@Composable
fun PokemonRow(stat: Stat){
    Column(Modifier.fillMaxWidth().padding(8.dp)){
        Text(text = "${stat.name}: ${stat.value}")
        LinearProgressIndicator(
            progress = { stat.value / 100f },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}