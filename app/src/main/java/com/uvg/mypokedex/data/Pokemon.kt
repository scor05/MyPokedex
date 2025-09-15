package com.uvg.mypokedex.data

import kotlinx.serialization.Serializable

@Serializable
data class Pokemon(
    val id: Int,
    val name: String,
    val type: List<String>,
    val height: Float,
    val weight: Float,
    val stats: List<Stat>
)

@Serializable
data class Stat(
    val name: String,
    val value: Int
)

