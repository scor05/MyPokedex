package com.uvg.mypokedex.data.local.db


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {
    @Query("SELECT * FROM cached_pokemon ORDER BY id ASC")
    fun listByNumberAsc(): Flow<List<CachedPokemon>>

    @Query("SELECT * FROM cached_pokemon ORDER BY id DESC")
    fun listByNumberDesc(): Flow<List<CachedPokemon>>

    @Query("SELECT * FROM cached_pokemon ORDER BY name COLLATE NOCASE ASC")
    fun listByNameAsc(): Flow<List<CachedPokemon>>

    @Query("SELECT * FROM cached_pokemon ORDER BY name COLLATE NOCASE DESC")
    fun listByNameDesc(): Flow<List<CachedPokemon>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CachedPokemon>)

    @Query("DELETE FROM cached_pokemon")
    suspend fun clearAll()
}