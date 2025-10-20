package com.uvg.mypokedex.data.remote.dto



import kotlinx.serialization.Serializable

@Serializable
data class TypeSlotDto(
    val type: TypeNameDto
)

@Serializable
data class TypeNameDto(
    val name: String
)

@Serializable
data class StatDto(
    val base_stat: Int,
    val stat: StatNameDto
)

@Serializable
data class StatNameDto(
    val name: String
)
