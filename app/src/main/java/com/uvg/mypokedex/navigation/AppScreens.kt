package com.uvg.mypokedex.navigation

sealed class AppScreens(val route: String) {
    object HomeScreen : AppScreens("home_screen")
    object FavoriteScreen : AppScreens("favorite_screen")
    object DetailScreen : AppScreens("detail_screen")
}




