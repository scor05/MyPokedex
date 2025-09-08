package com.uvg.mypokedex.ui.features.home

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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

@Serializable
data class Range(
    val start: Int,
    val end: Int
)

@Serializable
data class PokemonPage(
    val range: Range,
    val items: List<Pokemon>
)

class HomeViewModel(private val context: Context) : ViewModel() {

    private val _pokemons = mutableStateListOf<Pokemon>()
    val pokemons: List<Pokemon> get() = _pokemons

    private var currentPage = 0
    private var endReached = false

    // Genera nombres como "pokemon_001_10.json"
    private fun generateFileName(page: Int): String {
        val start = page * 10 + 1
        val end = (page + 1) * 10
        return "pokemon_${start.toString().padStart(3, '0')}_${end}.json"
    }

    fun loadMorePokemon() {
        if (endReached) return

        val pageToLoad = currentPage
        val fileName = generateFileName(pageToLoad)

        try {
            val jsonString = context.assets.open(fileName)
                .bufferedReader()
                .use { it.readText() }

            val page = Json.decodeFromString<PokemonPage>(jsonString)

            _pokemons.addAll(page.items)
            currentPage++

            android.util.Log.d("HVM", "Cargados ${page.items.size} pokémon. Total: ${_pokemons.size}")
        } catch (e: java.io.FileNotFoundException) {
            endReached = true
            android.util.Log.d("HVM", "No se encontró $fileName (fin de datos)")
        } catch (e: Exception) {
            android.util.Log.e("HVM", "Error cargando $fileName: ${e.message}", e)
        }
    }
}
