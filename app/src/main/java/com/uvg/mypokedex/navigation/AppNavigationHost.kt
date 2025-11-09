package com.uvg.mypokedex.navigation

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uvg.mypokedex.ui.features.favorites.FavoritesScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.uvg.mypokedex.ui.detail.DetailUI
import com.uvg.mypokedex.ui.detail.TopBar
import com.uvg.mypokedex.ui.detail.TopBarBackOnly
import com.uvg.mypokedex.ui.features.auth.AuthScreen
import com.uvg.mypokedex.ui.features.exchangepackage.ExchangeScreen
import com.uvg.mypokedex.ui.features.home.HomeScreen
import com.uvg.mypokedex.ui.features.home.HomeViewModel
import com.uvg.mypokedex.ui.features.auth.AuthUIState
import com.uvg.mypokedex.ui.features.auth.AuthViewModel

@Composable
fun AppNavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    favoriteToggled: Boolean
) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()

    // Determinar la ruta inicial basada en el estado de autenticación
    val startDestination = when (authState) {
        is AuthUIState.Authenticated -> AppScreens.HomeScreen.route
        else -> "auth"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // AUTH SCREEN
        composable("auth") {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(AppScreens.HomeScreen.route) {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

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

        // FAVORITES
        composable(AppScreens.Favorites.route) {
            Scaffold(
                topBar = {
                    TopBarBackOnly(
                        navController = navController,
                        title = "Favoritos"
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    FavoritesScreen(
                        onNavigateBack = { navController.navigateUp() },
                        onPokemonClick = { pokemonId ->
                            // Buscar el nombre del Pokémon por ID
                            navController.navigate("detail/pokemon-$pokemonId")
                        }
                    )
                }
            }
        }

        // EXCHANGE
        composable(AppScreens.Exchange.route) {
            ExchangeScreen(
                onNavigateBack = { navController.navigateUp() }
            )
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




//@Composable
//fun AppNavigationHost(
//    navController: NavHostController,
//    modifier: Modifier = Modifier,
//    homeViewModel: HomeViewModel,
//    favoriteToggled: Boolean
//) {
//    val navController = rememberNavController()
//
//    NavHost(
//        navController = navController,
//        startDestination = AppScreens.HomeScreen.route,
//        modifier = modifier
//    ) {
//        // HOME
//        composable(AppScreens.HomeScreen.route) {
//            Scaffold(
//                topBar = {
//                    TopBar(
//                        navController = navController,
//                        title = "MyPokedex",
//                        homeViewModel = homeViewModel
//                    )
//                }
//            ) { innerPadding ->
//                HomeScreen(
//                    navController = navController,
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(innerPadding),
//                    viewModel = homeViewModel,
//                    isFavorite = favoriteToggled
//                )
//            }
//        }
//
//        //FAVORITES
//        composable(AppScreens.Favorites.route) {
//            FavoritesScreen(
//                onNavigateBack = { navController.navigateUp() },
//                onPokemonClick = { pokemonId ->
//                    navController.navigate("detail/$pokemonId")
//                }
//            )
//        }
//
//        composable("auth") {
//            AuthScreen(onAuthSuccess = { navController.navigate("home"){ popUpTo("auth"){ inclusive = true } } })
//        }
//
//        composable(AppScreens.Exchange.route) {
//            ExchangeScreen(
//                onNavigateBack = { navController.navigateUp() }
//            )
//        }
//
//        // DETAIL
//        composable(
//            route = AppScreens.DetailScreen.route,
//            arguments = listOf(navArgument("pokemonName") { type = NavType.StringType })
//        ) { backStackEntry ->
//            val pokemonName =
//                backStackEntry.arguments?.getString("pokemonName") ?: return@composable
//
//            Scaffold(
//                topBar = {
//                    TopBarBackOnly(
//                        navController = navController,
//                        title = "Detalle"
//                    )
//                }
//            ) { innerPadding ->
//                Box(
//                    Modifier
//                        .fillMaxSize()
//                        .padding(innerPadding)
//                ) {
//                    DetailUI(
//                        pokemonName = pokemonName,
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(10.dp)
//                    )
//                }
//            }
//        }
//    }
//}