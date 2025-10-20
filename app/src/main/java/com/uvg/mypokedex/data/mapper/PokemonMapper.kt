package com.uvg.mypokedex.data.mapper

import com.uvg.mypokedex.data.Pokemon
import com.uvg.mypokedex.data.Stat
import com.uvg.mypokedex.data.remote.dto.*

fun PokemonDetailDTO.toDomain(): Pokemon {
    return Pokemon(
        id = id,
        name = name,
        height = height / 10f,
        weight = weight / 10f,
        type = types.map { it.type.name },
        stats = stats.map { Stat(name = it.stat.name, value = it.base_stat) }
    )
}