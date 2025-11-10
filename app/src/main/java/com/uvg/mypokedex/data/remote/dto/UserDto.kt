package com.uvg.mypokedex.data.remote.dto

data class UserDto(
    val uid: String = "",
    val displayName: String = "",
    val email: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val favoritePokemonIds: List<Int> = emptyList()
)