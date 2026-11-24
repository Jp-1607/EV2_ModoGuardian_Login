package com.example.vicente_parrra.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object Espacio {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val tactil = 48.dp
    val linea = 1.dp
    val foco = 2.dp
    val sombra = 3.dp
}

private val Colores = lightColorScheme(
    primary = Pino,
    onPrimary = Papel,
    primaryContainer = Salvia,
    onPrimaryContainer = Pino,
    secondary = Cobre,
    onSecondary = Color.White,
    secondaryContainer = Arcilla,
    onSecondaryContainer = Cobre,
    tertiary = Ocre,
    onTertiary = Papel,
    tertiaryContainer = Arena,
    onTertiaryContainer = Ocre,
    background = Marfil,
    onBackground = Tinta,
    surface = Papel,
    onSurface = Tinta,
    surfaceVariant = Salvia,
    onSurfaceVariant = TintaSuave,
    surfaceTint = Color.Transparent,
    outline = Borde,
    outlineVariant = Borde,
    error = Granate,
    onError = Color.White,
    errorContainer = Arcilla,
    onErrorContainer = Granate
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(14.dp)
)

@Composable
fun Vicente_parrraTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colores, typography = Typography, shapes = Formas, content = content)
}
