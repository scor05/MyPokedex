package com.uvg.mypokedex.data

data class Pokemon(
    val id: Int,
    val name: String,
    val type: List<String>,
    val weight: Float,
    val stats: List<Stat>,
    val height: Float,
)
data class Stat(
    val name: String,
    val value: Int
)