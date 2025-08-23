package com.uvg.mypokedex.ui.detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.uvg.mypokedex.data.Stat

@Composable
fun Stat.StatBar() {
    LinearProgressIndicator(
        progress = value / 100f,
        modifier = Modifier.fillMaxWidth()
    )
}