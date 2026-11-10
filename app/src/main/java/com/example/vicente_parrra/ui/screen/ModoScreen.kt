package com.example.vicente_parrra.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vicente_parrra.viewmodel.ModoViewModel

@Composable
fun ModoScreen(modelo: ModoViewModel) {
    val activo by modelo.activo.collectAsState()
    val mensaje by modelo.mensaje.collectAsState()
    val texto by remember { derivedStateOf { if (activo == true) "Desactivar modo" else "Activar modo" } }
    val colores = MaterialTheme.colorScheme
    val color by animateColorAsState(if (activo == true) colores.tertiary else colores.primary, label = "Modo")
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Modo especial")
        if (activo == null) {
            CircularProgressIndicator()
        } else {
            Text(if (activo == true) "Activado" else "Desactivado")
            Button(onClick = modelo::cambiarModo,
                colors = ButtonDefaults.buttonColors(containerColor = color,
                    contentColor = if (activo == true) colores.onTertiary else colores.onPrimary)) {
                Text(texto)
            }
        }
        AnimatedVisibility(mensaje.isNotEmpty()) { Text(mensaje) }
    }
}
