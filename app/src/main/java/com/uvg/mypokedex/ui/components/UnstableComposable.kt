// ui/components/UnstableComposable.kt
package com.uvg.mypokedex.ui.components
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

// PREGUNTAS POR HACERSE:
// 1. ¿No es idempotente?
// 2. ¿Tiene efectos secundarios?
// 3. ¿Usa un tipo de dato inestable (List) como parámetro?

@Composable
fun UnstablePokemonList(pokemons: List<String>) {
    // remember hace que se guarde en caché el color y no se calcule por cada recomposición
    val colorRand = remember { Color(Random.nextInt(0, 0xFFFFFF)) }
    val randomColor = String.format("#%06x", colorRand)

    // SideEffect hace que se ejecute código después de haber dibujado toda la UI, lo cual elimina
    // todos los prints que se hacen por cada composición y solo muestra uno.
    SideEffect { println("Composing UnstablePokemonList with color $randomColor") }
    Button(onClick = { /* no hace nada, solo para forzar recomposición */ }, modifier = Modifier.background(colorRand)) {
        Text(text = "Tengo ${pokemons.size} Pokémon favoritos", modifier = Modifier.background(colorRand))
    }
}