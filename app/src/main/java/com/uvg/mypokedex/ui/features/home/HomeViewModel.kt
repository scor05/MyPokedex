package com.uvg.mypokedex.ui.features.home

import android.content.Context

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.uvg.mypokedex.data.Pokemon

class HomeViewModel(private val context: Context) {

    private var currentPage = 0
    private var endReached = false

    private val _pokemons = mutableStateListOf<Pokemon>()
    val pokemons: SnapshotStateList<Pokemon> get() = _pokemons

    private val _favoritePokemons = mutableStateListOf<String>()

    fun loadMorePokemon() {
        if (endReached) return


        val pageToLoad = currentPage + 1
        val fileName = generateFileName(pageToLoad)

        try {

            val allAssets = context.assets.list("")?.toList() ?: emptyList()
            android.util.Log.d("HVM", "Assets disponibles: $allAssets")
            android.util.Log.d("HVM", "Intentando abrir: $fileName")

            val jsonString = context.assets.open(fileName)
                .bufferedReader()
                .use { it.readText() }

            val jsonObject = org.json.JSONObject(jsonString)
            val results = jsonObject.getJSONArray("items")
            val newBatch = mutableListOf<com.uvg.mypokedex.data.Pokemon>()

            for (i in 0 until results.length()) {
                val p = results.getJSONObject(i)
                val id = p.getInt("id")
                val name = p.getString("name")
                val height = p.getDouble("height").toFloat()
                val weight = p.getDouble("weight").toFloat()

                val typesArray = p.getJSONArray("type")
                val types = MutableList(typesArray.length()) { j -> typesArray.getString(j) }

                val statsArray = p.getJSONArray("stats")
                val stats = mutableListOf<com.uvg.mypokedex.data.Stat>()
                for (j in 0 until statsArray.length()) {
                    val s = statsArray.getJSONObject(j)
                    stats.add(com.uvg.mypokedex.data.Stat(
                        name = s.getString("name"),
                        value = s.getInt("value")
                    ))
                }

                newBatch.add(com.uvg.mypokedex.data.Pokemon( id =  id, name =name, type = types, height = height, weight = weight, stats = stats))
            }


            _pokemons.addAll(newBatch)
            currentPage = pageToLoad

            android.util.Log.d("HVM", "Cargados ${newBatch.size} pokémon. Total: ${_pokemons.size}")

        } catch (e: java.io.FileNotFoundException) {
            endReached = true
            android.util.Log.d("HVM", "No se encontró $fileName (fin de datos)")
        } catch (e: Exception) {
            android.util.Log.e("HVM", "Error cargando $fileName: ${e.message}", e)
        }
    }
    fun toggleFavorite(pokemonName: String) {
        if (_favoritePokemons.contains(pokemonName)) {
            _favoritePokemons.remove(pokemonName)
        } else {
            _favoritePokemons.add(pokemonName)
        }
    }

    fun isFavorite(pokemonName: String): Boolean {
        return _favoritePokemons.contains(pokemonName)
    }
}

private fun generateFileName(page: Int): String {
    val itemsPerPage = 10
    val bottom = (page - 1) * itemsPerPage + 1
    val top = page * itemsPerPage
    val fb = String.format(java.util.Locale.US, "%03d", bottom)
    val ft = String.format(java.util.Locale.US, "%03d", top)
    return "pokemon_${fb}_${ft}.json"
}