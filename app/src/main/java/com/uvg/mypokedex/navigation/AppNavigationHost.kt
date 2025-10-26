package com.uvg.mypokedex.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.uvg.mypokedex.ui.detail.DetailUI
import com.uvg.mypokedex.ui.detail.TopBar
import com.uvg.mypokedex.ui.detail.TopBarBackOnly
import com.uvg.mypokedex.ui.features.home.HomeScreen
import com.uvg.mypokedex.ui.features.home.HomeViewModel

@Composable
fun AppNavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    favoriteToggled: Boolean
) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.HomeScreen.route,
        modifier = modifier
    ) {
        // HOME
        composable(AppScreens.HomeScreen.route) {
            Scaffold(
                topBar = {
                    TopBar(
                        navController = navController,
                        title = "MyPokedex",
                        homeViewModel = homeViewModel
                    )
                }
            ) { innerPadding ->
                HomeScreen(
                    navController = navController,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    viewModel = homeViewModel,
                    isFavorite = favoriteToggled
                )
            }
        }

        // DETAIL
        composable(
            route = AppScreens.DetailScreen.route,
            arguments = listOf(navArgument("pokemonName") { type = NavType.StringType })
        ) { backStackEntry ->
            val pokemonName =
                backStackEntry.arguments?.getString("pokemonName") ?: return@composable

            Scaffold(
                topBar = {
                    TopBarBackOnly(
                        navController = navController,
                        title = "Detalle"
                    )
                }
            ) { innerPadding ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    DetailUI(
                        pokemonName = pokemonName,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    )
                }
            }
        }
    }
}