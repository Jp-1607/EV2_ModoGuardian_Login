package com.example.vicente_parrra.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.vicente_parrra.model.EstadoPedido
import com.example.vicente_parrra.ui.theme.Arena
import com.example.vicente_parrra.ui.theme.Cobre
import com.example.vicente_parrra.ui.theme.Espacio
import com.example.vicente_parrra.ui.theme.Musgo
import com.example.vicente_parrra.ui.theme.Ocre
import com.example.vicente_parrra.ui.theme.Pino
import com.example.vicente_parrra.ui.theme.Salvia
import java.text.NumberFormat
import java.util.Locale

@Composable
fun Pantalla(titulo: String, detalle: String? = null, contenido: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 1040.dp).fillMaxSize(),
            contentPadding = PaddingValues(Espacio.xl),
            verticalArrangement = Arrangement.spacedBy(Espacio.l)
        ) {
            item {
                Text(titulo, Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineLarge, color = Pino)
                detalle?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Espacio.l), content = contenido)
            }
        }
    }
}

@Composable
fun Panel(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(Espacio.linea, MaterialTheme.colorScheme.outline),
        shadowElevation = Espacio.sombra
    ) {
        Column(Modifier.padding(Espacio.l), verticalArrangement = Arrangement.spacedBy(Espacio.s), content = contenido)
    }
}

@Composable
fun Accion(
    etiqueta: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitada: Boolean = true,
    secundaria: Boolean = false
) {
    var enfocada by remember { mutableStateOf(false) }
    val interaccion = remember { MutableInteractionSource() }
    val encima by interaccion.collectIsHoveredAsState()
    val borde = when {
        enfocada -> Cobre
        encima && habilitada -> Pino.copy(alpha = 0.45f)
        else -> Color.Transparent
    }
    val colorFoco by animateColorAsState(borde, label = "Foco de botón")
    val forma = MaterialTheme.shapes.small
    val control = modifier.defaultMinSize(minHeight = Espacio.tactil)
        .hoverable(interaccion, enabled = habilitada)
        .onFocusChanged { enfocada = it.hasFocus }
        .border(Espacio.foco, colorFoco, forma)
    if (secundaria) {
        OutlinedButton(onClick, control, enabled = habilitada, shape = forma) { Text(etiqueta) }
    } else {
        Button(
            onClick, control, enabled = habilitada, shape = forma,
            colors = ButtonDefaults.buttonColors(containerColor = Pino)
        ) { Text(etiqueta) }
    }
}

@Composable
fun EstadoVisual(estado: EstadoPedido) {
    val fondo = when (estado) {
        EstadoPedido.PENDIENTE -> Arena
        EstadoPedido.EN_RUTA -> Salvia
        EstadoPedido.ENTREGADO -> Musgo
    }
    val texto = when (estado) {
        EstadoPedido.PENDIENTE -> Ocre
        EstadoPedido.EN_RUTA -> Pino
        EstadoPedido.ENTREGADO -> Color.White
    }
    Surface(color = fondo, contentColor = texto, shape = MaterialTheme.shapes.extraSmall) {
        Text(estado.etiqueta, Modifier.padding(horizontal = Espacio.s, vertical = Espacio.xs), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun Mensajes(
    cargando: Boolean,
    sinConexion: Boolean,
    actualizada: Long?,
    error: String?,
    aviso: String?,
    actualizar: () -> Unit
) {
    AnimatedVisibility(cargando || sinConexion || error != null || aviso != null) {
        Column(
            Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
            verticalArrangement = Arrangement.spacedBy(Espacio.xs)
        ) {
            if (cargando) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (sinConexion) {
                Surface(color = Arena, contentColor = Ocre) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = Espacio.l, vertical = Espacio.xs),
                        horizontalArrangement = Arrangement.spacedBy(Espacio.s),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (actualizada == null) "Sin conexión con la API." else "Sin conexión · datos guardados",
                            Modifier.weight(1f), style = MaterialTheme.typography.labelMedium
                        )
                        Accion("Reintentar", actualizar, habilitada = !cargando, secundaria = true)
                    }
                }
            }
            if (error != null) Text(error, Modifier.padding(horizontal = Espacio.l), color = MaterialTheme.colorScheme.error)
            if (aviso != null) Text(aviso, Modifier.padding(horizontal = Espacio.l), color = Musgo)
        }
    }
}

fun moneda(pesos: Long): String = "$" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CL")).format(pesos)

@Composable
fun Opcion(etiqueta: String, marcada: Boolean, habilitada: Boolean, cambiar: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().defaultMinSize(minHeight = Espacio.tactil)
            .toggleable(marcada, enabled = habilitada, role = Role.Checkbox, onValueChange = cambiar),
        horizontalArrangement = Arrangement.spacedBy(Espacio.s),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(marcada, onCheckedChange = null, enabled = habilitada)
        Text(etiqueta)
    }
}
