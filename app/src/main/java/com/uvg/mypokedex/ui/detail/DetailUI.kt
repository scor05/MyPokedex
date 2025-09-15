@file:OptIn(ExperimentalMaterial3Api::class)

package com.uvg.mypokedex.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.uvg.mypokedex.data.*
import com.uvg.mypokedex.ui.components.FavoriteButton
import com.uvg.mypokedex.ui.features.home.HomeViewModel
import com.uvg.mypokedex.ui.search.SearchToolsDialog
import java.util.Locale




@Composable
fun DetailUI(
    pokemonName: String,
    homeViewModel: HomeViewModel,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = pokemonName.replaceFirstChar { it.uppercaseChar() },
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 50.sp
        )

        Spacer(modifier = Modifier.height(3.dp))
        AsyncImage(
            model = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${homeViewModel.getPokemon(pokemonName).id}.png",
            contentDescription = "Imagen de ${pokemonName}",
            modifier = Modifier.size(240.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        for (stat in homeViewModel.getPokemon(pokemonName).stats) {
            PokemonRow(stat)
        }
    }
}

@Composable
fun TopBar(navController: NavController, title: String) {

    var showTools by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(text = title, textAlign = TextAlign.Center)
                if (showTools) {
                    SearchToolsDialog(
                        onDismiss = { showTools = false }
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",

                    )
                }
            },
            actions = {
                IconButton(onClick = { showTools = true }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Herramientas"
                    )
                }
            }
        )
    }
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
    Column(Modifier
        .fillMaxWidth()
        .padding(8.dp)){
        Text(text = "${stat.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }}: ${stat.value}", fontSize = 18.sp)
        LinearProgressIndicator(
            progress = { stat.value / 100f },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}