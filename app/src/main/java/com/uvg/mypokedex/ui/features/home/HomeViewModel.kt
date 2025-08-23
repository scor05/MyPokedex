package com.uvg.mypokedex.ui.features.home

import com.uvg.mypokedex.data.Pokemon
import com.uvg.mypokedex.data.Stat

class HomeViewModel{
    fun getPokemonList(): List<Pokemon>{
        return listOf(
            Pokemon(3, "Ivysaur", listOf("electrico"), 9.45f, listOf(
                Stat("HP", 35) ,Stat("Attack", 55),Stat("Defense", 40),Stat("Speed", 90)), 0.9f)
        )
    }
}