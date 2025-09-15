package com.uvg.mypokedex.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material.icons.sharp.KeyboardArrowUp
import androidx.compose.material.icons.sharp.KeyboardArrowDown
import androidx.compose.runtime.Composable

@Composable
fun PokemonOrderButton(currentState: Boolean, onClick: () -> Unit){
    SmallFloatingActionButton(
        onClick = {
            onClick()
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        if (currentState) {
            Icon(Icons.Sharp.KeyboardArrowUp, "Ordenado Ascendentemente")
        } else {
            Icon(Icons.Sharp.KeyboardArrowDown, "Ordenado Descendentemente")
        }
    }
}