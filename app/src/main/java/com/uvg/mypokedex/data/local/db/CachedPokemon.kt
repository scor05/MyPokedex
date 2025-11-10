package com.uvg.mypokedex.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "cached_pokemon")
@TypeConverters(Converters::class)
data class CachedPokemon(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String>,
    val stats: Map<String, Int>,
    val lastFetchedAt: Long
)