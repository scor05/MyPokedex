package com.uvg.mypokedex.ui.features.home

import androidx.compose.runtime.mutableStateListOf
import com.uvg.mypokedex.data.Pokemon
import com.uvg.mypokedex.data.Stat

class HomeViewModel{
    fun getPokemonList(): List<Pokemon>{
        return listOf(
            Pokemon(
                id = 1,
                name = "bulbasaur",
                type = listOf("grass", "poison"),
                weight = 6.9f,
                height = 0.7f,
                stats = listOf(
                    Stat(name = "hp", value = 45),
                    Stat(name = "attack", value = 49),
                    Stat(name = "defense", value = 49),
                    Stat(name = "special-attack", value = 65),
                    Stat(name = "special-defense", value = 65),
                    Stat(name = "speed", value = 45)
                )
            ),
            Pokemon(
                id = 4,
                name = "charmander",
                type = listOf("fire"),
                weight = 8.5f,
                height = 0.6f,
                stats = listOf(
                    Stat(name = "hp", value = 39),
                    Stat(name = "attack", value = 52),
                    Stat(name = "defense", value = 43),
                    Stat(name = "special-attack", value = 60),
                    Stat(name = "special-defense", value = 50),
                    Stat(name = "speed", value = 65)
                )
            ),
            Pokemon(
                id = 7,
                name = "squirtle",
                type = listOf("water"),
                weight = 9.0f,
                height = 0.5f,
                stats = listOf(
                    Stat(name = "hp", value = 44),
                    Stat(name = "attack", value = 48),
                    Stat(name = "defense", value = 65),
                    Stat(name = "special-attack", value = 50),
                    Stat(name = "special-defense", value = 64),
                    Stat(name = "speed", value = 43)
                )
            ),
            Pokemon(
                id = 25,
                name = "pikachu",
                type = listOf("electric"),
                weight = 6.0f,
                height = 0.4f,
                stats = listOf(
                    Stat(name = "hp", value = 35),
                    Stat(name = "attack", value = 55),
                    Stat(name = "defense", value = 40),
                    Stat(name = "special-attack", value = 50),
                    Stat(name = "special-defense", value = 50),
                    Stat(name = "speed", value = 90)
                )
            ),
            Pokemon(
                id = 133,
                name = "eevee",
                type = listOf("normal"),
                weight = 6.5f,
                height = 0.3f,
                stats = listOf(
                    Stat(name = "hp", value = 55),
                    Stat(name = "attack", value = 55),
                    Stat(name = "defense", value = 50),
                    Stat(name = "special-attack", value = 45),
                    Stat(name = "special-defense", value = 65),
                    Stat(name = "speed", value = 55)
                )
            ),
            Pokemon(
                id = 39,
                name = "jigglypuff",
                type = listOf("normal", "fairy"),
                weight = 5.5f,
                height = 0.5f,
                stats = listOf(
                    Stat(name = "hp", value = 115),
                    Stat(name = "attack", value = 45),
                    Stat(name = "defense", value = 20),
                    Stat(name = "special-attack", value = 45),
                    Stat(name = "special-defense", value = 25),
                    Stat(name = "speed", value = 20)
                )
            ),
            Pokemon(
                id = 52,
                name = "meowth",
                type = listOf("normal"),
                weight = 4.2f,
                height = 0.4f,
                stats = listOf(
                    Stat(name = "hp", value = 40),
                    Stat(name = "attack", value = 45),
                    Stat(name = "defense", value = 35),
                    Stat(name = "special-attack", value = 40),
                    Stat(name = "special-defense", value = 40),
                    Stat(name = "speed", value = 90)
                )
            ),
            Pokemon(
                id = 54,
                name = "psyduck",
                type = listOf("water"),
                weight = 19.6f,
                height = 0.8f,
                stats = listOf(
                    Stat(name = "hp", value = 50),
                    Stat(name = "attack", value = 52),
                    Stat(name = "defense", value = 48),
                    Stat(name = "special-attack", value = 65),
                    Stat(name = "special-defense", value = 50),
                    Stat(name = "speed", value = 55)
                )
            ),
            Pokemon(
                id = 143,
                name = "snorlax",
                type = listOf("normal"),
                weight = 460.0f,
                height = 2.1f,
                stats = listOf(
                    Stat(name = "hp", value = 160),
                    Stat(name = "attack", value = 110),
                    Stat(name = "defense", value = 65),
                    Stat(name = "special-attack", value = 65),
                    Stat(name = "special-defense", value = 110),
                    Stat(name = "speed", value = 30)
                )
            ),
            Pokemon(
                id = 150,
                name = "mewtwo",
                type = listOf("psychic"),
                weight = 122.0f,
                height = 2.0f,
                stats = listOf(
                    Stat(name = "hp", value = 106),
                    Stat(name = "attack", value = 110),
                    Stat(name = "defense", value = 90),
                    Stat(name = "special-attack", value = 154),
                    Stat(name = "special-defense", value = 90),
                    Stat(name = "speed", value = 130)
                )
            )
        )
    }

private val _favoritePokemons = mutableStateListOf<String>()
val favoritePokemons: List<String> get() = _favoritePokemons

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
