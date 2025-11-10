package com.uvg.mypokedex.data.remote.dto

data class ExchangeDto(
    val exchangeId: String = "",
    val userAId: String = "",
    val userAName: String = "",
    val userBId: String = "",
    val userBName: String = "",
    val pokemonAId: Int = 0,
    val pokemonAName: String = "",
    val pokemonBId: Int = 0,
    val pokemonBName: String = "",
    val status: ExchangeStatus = ExchangeStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

enum class ExchangeStatus {
    PENDING,
    ACCEPTED,
    COMPLETED,
    CANCELLED,
    TIMEOUT
}