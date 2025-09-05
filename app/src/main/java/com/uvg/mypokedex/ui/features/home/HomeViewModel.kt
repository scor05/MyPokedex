package com.uvg.mypokedex.ui.features.home

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.uvg.mypokedex.data.Pokemon
import com.uvg.mypokedex.data.Stat
import java.util.Locale

private val _favoritePokemons = mutableStateListOf<String>()
class HomeViewModel(context: Context) {

    private var currentPage = 0

    val currentJson = generateFileName(currentPage)

    fun loadPokemonFromJson(): List<Pokemon> {
        return emptyList() // TODO: hacerlo
    }
}

fun generateFileName(currentPage: Int): String{
    val topLimit = 10 + 10 * currentPage
    val bottomLimit = topLimit - 9
    val formattedBottomLimit = String.format(Locale.US, "%03d", bottomLimit)
    val formattedTopLimit = String.format(Locale.US, "%03d", topLimit)
    return "pokemon_${formattedBottomLimit}_$formattedTopLimit.json"
}

fun toggleFavorite(pokemonName: String) {
    if (_favoritePokemons.contains(pokemonName)) {
        _favoritePokemons.remove(pokemonName)
    } else {
        _favoritePokemons.add(pokemonName)
    }
}

fun isFavorite(pokemonName: String): Boolean {
    return _favoritePokemons.contains(pokemonName)
}

