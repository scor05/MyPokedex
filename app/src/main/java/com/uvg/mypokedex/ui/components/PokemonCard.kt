package com.uvg.mypokedex.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.uvg.mypokedex.data.remote.dto.PokemonResult

@Composable
fun PokemonCard(
    pokemon: PokemonResult,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit
) {
    val id = extractIdFromUrl(pokemon.url)
    val idFormatted = id.padStart(3, '0')
    val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"

    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(3.dp, Color.LightGray, shape = MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onItemClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Imagen de ${pokemon.name}",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = pokemon.name.replaceFirstChar { it.uppercaseChar() },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )

            // Número de Pokédex
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "#$idFormatted",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FavoriteButton(
                isFavorite = isFavorite,
                onToggleFavorite = { onToggleFavorite() }
            )
        }
    }
}

fun extractIdFromUrl(url: String): String {
    return url.trimEnd('/').split("/").last()
}