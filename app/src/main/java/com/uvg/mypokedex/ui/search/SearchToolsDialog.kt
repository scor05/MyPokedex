package com.uvg.mypokedex.ui.search

import android.text.Layout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel


enum class SortOption { Numero, Nombre }

@Composable
fun SearchToolsDialog(
    selected: SortOption,
    ascending: Boolean,
    favorites: Boolean,
    onSelectedChange: (SortOption) -> Unit,
    onAscendingChange: (Boolean) -> Unit,
    onFavoriteChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Herramientas de búsqueda") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Column {
                    Text("Ordenar por")
                    Box {
                        OutlinedButton(onClick = { expanded = true }) {
                            Text(
                                when (selected) {
                                    SortOption.Numero -> "Número de Pokedex"
                                    SortOption.Nombre -> "Nombre"
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Número") },
                                onClick = {
                                    onSelectedChange(SortOption.Numero)
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Nombre") },
                                onClick = {
                                    onSelectedChange(SortOption.Nombre)
                                    expanded = false
                                }
                            )
                        }
                    }
                }


                Column {
                    Text("Orden")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = ascending,
                                onClick = { onAscendingChange(true) }
                            )
                            Text("Ascendente")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = !ascending,
                                onClick = { onAscendingChange(false) }
                            )
                            Text("Descendente")
                        }
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Mostrar favoritos?")
                        Checkbox(
                            checked = favorites,
                            onCheckedChange = onFavoriteChange
                        )
                    }
                }


            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}