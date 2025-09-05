package com.uvg.mypokedex.ui.features.home

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.uvg.mypokedex.data.Pokemon
import com.uvg.mypokedex.data.Stat

class HomeViewModel(context: Context) {

    fun loadPokemonFromJson(): List<Pokemon> {
        return emptyList() // TODO: hacerlo
    }
}
private val _favoritePokemons = mutableStateListOf<String>()

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
}
