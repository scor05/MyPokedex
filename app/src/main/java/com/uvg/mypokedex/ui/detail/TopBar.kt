package com.uvg.mypokedex.ui.detail

import androidx.activity.compose.LocalActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.uvg.mypokedex.navigation.AppScreens
import com.uvg.mypokedex.ui.components.AuthDialog
import com.uvg.mypokedex.ui.features.auth.AuthViewModel
import com.uvg.mypokedex.ui.features.home.HomeViewModel
import com.uvg.mypokedex.ui.search.SearchToolsDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavController,
    title: String,
    homeViewModel: HomeViewModel
) {
    var showTools by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    val activity = LocalActivity.current
    val authViewModel: AuthViewModel = viewModel()
    val isAuthenticated = authViewModel.isAuthenticated()

    CenterAlignedTopAppBar(
        title = { Text(text = title, textAlign = TextAlign.Center) },
        navigationIcon = {
            IconButton(onClick = {
                val popped = navController.popBackStack()
                if (!popped) {
                    activity?.moveTaskToBack(true)
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Atrás"
                )
            }
        },
        actions = {
            // Botón de Favoritos
            IconButton(
                onClick = {
                    if (isAuthenticated) {
                        navController.navigate(AppScreens.Favorites.route)
                    } else {
                        showAuthDialog = true
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Favoritos",
                    tint = if (isAuthenticated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Botón de Intercambio
            IconButton(
                onClick = {
                    if (isAuthenticated) {
                        navController.navigate(AppScreens.Exchange.route)
                    } else {
                        showAuthDialog = true
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.SwapHoriz,
                    contentDescription = "Intercambio",
                    tint = if (isAuthenticated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Botón de cerrar sesión (solo si está autenticado)
            if (isAuthenticated) {
                IconButton(onClick = {
                    authViewModel.signOut()
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.ExitToApp,
                        contentDescription = "Cerrar Sesión"
                    )
                }
            }

            // Botón de configuraciones
            IconButton(onClick = { showTools = true }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Configuraciones"
                )
            }
        }
    )

    // Dialog de autenticación
    if (showAuthDialog) {
        AuthDialog(
            onDismiss = { showAuthDialog = false },
            onAuthSuccess = {
                showAuthDialog = false
                // No navegar aquí, solo cerrar el dialog
            }
        )
    }

    // Dialog de herramientas de búsqueda
    if (showTools) {
        SearchToolsDialog(
            favorites = homeViewModel.favoritesToggle,
            onFavoriteChange = { homeViewModel.setFavoritesOnly(it) },
            selected = homeViewModel.sortOption,
            ascending = homeViewModel.ascending,
            onSelectedChange = { homeViewModel.setSortOptionCustom(it) },
            onAscendingChange = { homeViewModel.setAscendingCustom(it) },
            onDismiss = { showTools = false }
        )
    }
}
