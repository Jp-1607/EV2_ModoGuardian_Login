package com.example.vicente_parrra.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

enum class WindowSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED
}

@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val ancho = LocalConfiguration.current.screenWidthDp

    // Puntos de quiebre recomendados para celular, tablet y pantallas amplias.
    return when {
        ancho < 600 -> WindowSizeClass.COMPACT
        ancho < 840 -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.EXPANDED
    }
}

