package com.uvg.mypokedex.ui.features.home

import com.uvg.mypokedex.data.Pokemon

class HomeViewModel{
    fun getPokemonList(): List<Pokemon>{
        return listOf(
            Pokemon(1, "bulbasaur"),
            Pokemon(4, "Charmander"),
            Pokemon(150, "Mewtwo")
        )
    }
}