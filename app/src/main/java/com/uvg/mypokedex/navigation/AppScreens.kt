package com.uvg.mypokedex.navigation


sealed class AppScreens(val route: String) {

    object HomeScreen : AppScreens("home")

    object DetailScreen : AppScreens("detail/{pokemonName}") {
        fun createRoute(pokemonName: String) = "detail/$pokemonName"
    }
    object SearchToolsDialog : AppScreens("search_tools_dialog")

    data object Favorites : AppScreens("favorites")
}