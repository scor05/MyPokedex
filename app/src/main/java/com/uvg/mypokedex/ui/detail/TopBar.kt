package com.uvg.mypokedex.ui.detail

import androidx.activity.compose.LocalActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
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
    val activity = LocalActivity.current

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
            IconButton(onClick = { showTools = true }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Configuraciones"
                )
            }
        }
    )

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