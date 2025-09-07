package com.uvg.mypokedex.ui.features.home

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.add
import androidx.compose.runtime.mutableStateListOf
import com.uvg.mypokedex.data.Pokemon
import com.uvg.mypokedex.data.Stat
import kotlinx.serialization.*
import org.json.JSONObject
import java.util.Locale

private val _favoritePokemons = mutableStateListOf<String>()
class HomeViewModel(context: Context) {

    private var currentPage = 0


    fun loadMorePokemon(): List<Pokemon> {
        try {
            currentPage++
            val newJson = generateFileName(currentPage)
            val jsonObject = JSONObject(newJson)
            val results = jsonObject.getJSONArray("items")
            val pokemonList = mutableListOf<Pokemon>()

            for (i in 0 until results.length()) {
                val pokemonJson = results.getJSONObject(i)

                // los JSONObject tienen métodos get*TIPO*(string) para retornar
                // el dato correspondiente a esa etiqueta
                val id = pokemonJson.getInt("id")
                val name = pokemonJson.getString("name")
                val height = pokemonJson.getDouble("height").toFloat()
                val weight = pokemonJson.getDouble("weight").toFloat()

                val typesArray = pokemonJson.getJSONArray("types")
                val types = mutableListOf<String>()
                for (j in 0 until typesArray.length()) {
                    types.add(typesArray.getString(j))
                }

                val statsArray = pokemonJson.getJSONArray("stats")
                val stats = mutableListOf<Stat>()
                for (j in 0 until statsArray.length()) {
                    val statJson = statsArray.getJSONObject(j)
                    val statName = statJson.getString("name")
                    val statValue = statJson.getInt("value")
                    stats.add(Stat(name = statName, value = statValue))
                }

                val pokemon = Pokemon(
                    id = id,
                    name = name,
                    type = types,
                    height = height,
                    weight = weight,
                    stats = stats
                )
                pokemonList.add(pokemon)
            }

            return pokemonList
        } catch (e: Exception){
            Log.d("HomeViewModel", e.message.toString())
            return emptyList()
        }
    }
}

fun generateFileName(currentPage: Int): String{
    val topLimit = 10 + 10 * currentPage
    val bottomLimit = topLimit - 9
    val formattedBottomLimit = String.format(Locale.US, "%03d", bottomLimit)
    val formattedTopLimit = String.format(Locale.US, "%03d", topLimit)
    return "pokemon_${formattedBottomLimit}_$formattedTopLimit.json"
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

