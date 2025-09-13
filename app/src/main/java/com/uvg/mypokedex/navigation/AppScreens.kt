package com.uvg.mypokedex.navigation


sealed class AppScreens(val route: String) {

    object HomeScreen : AppScreens("home")

    object DetailScreen : AppScreens("detail/{itemId}") {
        fun createRoute(itemId: String) = "detail/$itemId"
    }
    object SearchToolsDialog : AppScreens("search_tools_dialog")
}