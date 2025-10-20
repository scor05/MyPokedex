package com.uvg.mypokedex.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PokemonListResponse(
    val results: List<PokemonItemDto>
)

@Serializable
data class PokemonItemDto(
    val name: String,
    val url: String
)