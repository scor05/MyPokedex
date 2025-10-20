package com.uvg.mypokedex.data.remote.dto

import kotlinx.serialization.Serializable


@Serializable
data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Float,
    val weight: Float,
    val types: List<TypeSlotDto>,
    val stats: List<StatDto>
)
