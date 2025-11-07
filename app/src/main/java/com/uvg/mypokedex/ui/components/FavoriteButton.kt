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
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onAuthRequired: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    favoritesViewModel: FavoritesViewModel = viewModel()
) {
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                try {
                    onToggleFavorite()
                } finally {
                    isLoading = false
                }
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
                tint = if (isFavorite) Color.Red else Color.Gray
            )
        }
    }
}