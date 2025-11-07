package com.uvg.mypokedex.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uvg.mypokedex.ui.features.auth.AuthViewModel
import com.uvg.mypokedex.ui.features.favorites.FavoritesViewModel
import kotlinx.coroutines.launch

@Composable
fun FavoriteButton(
    pokemonId: Int,
    pokemonName: String,
    imageUrl: String,
    onAuthRequired: () -> Unit, // Mostrar modal de login
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    favoritesViewModel: FavoritesViewModel = viewModel(),
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    IconButton(onClick = { onToggleFavorite() }) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
            tint = if (isFavorite) Color.Red else Color.Gray
        )
    }
    val scope = rememberCoroutineScope()
    var isFavorite by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Verificar si es favorito al cargar
    LaunchedEffect(pokemonId) {
        if (authViewModel.isAuthenticated()) {
            isFavorite = favoritesViewModel.isFavorite(pokemonId)
        }
    }

    IconButton(
        onClick = {
            // Verificar autenticación
            if (!authViewModel.isAuthenticated()) {
                onAuthRequired()
                return@IconButton
            }

            // Toggle favorito
            scope.launch {
                isLoading = true
                favoritesViewModel.toggleFavorite(pokemonId, pokemonName, imageUrl)
                isFavorite = !isFavorite
                isLoading = false
            }
        },
        modifier = modifier
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}