package com.uvg.mypokedex.data.remote.dto

data class FavoritePokemonDto(
    val pokemonId: Int = 0,
    val pokemonName: String = "",
    val imageUrl: String = "",
    val addedAt: Long = System.currentTimeMillis()
)