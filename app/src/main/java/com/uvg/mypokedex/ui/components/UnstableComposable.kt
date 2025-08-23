// ui/components/UnstableComposable.kt
package com.uvg.mypokedex.ui.components
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

// PREGUNTAS POR HACERSE:
// 1. ¿No es idempotente?
// 2. ¿Tiene efectos secundarios?
// 3. ¿Usa un tipo de dato inestable (List) como parámetro?

@Composable
fun UnstablePokemonList(pokemons: List<String>, colorNum: Int) {
    val randomColor = String.format("#%06x", colorNum)
    println("Composing UnstablePokemonList with color $randomColor")
    Button(onClick = { /* no hace nada, solo para forzar recomposición */ }, modifier = Modifier.background(Color(colorNum))) {
        Text(text = "Tengo ${pokemons.size} Pokémon favoritos", modifier = Modifier.background(Color(colorNum   )))
    }
}