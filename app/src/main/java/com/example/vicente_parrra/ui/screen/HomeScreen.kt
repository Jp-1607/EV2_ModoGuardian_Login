package com.example.vicente_parrra.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vicente_parrra.model.Indicadores
import com.example.vicente_parrra.model.Usuario
import com.example.vicente_parrra.navigation.Screen
import com.example.vicente_parrra.ui.components.Accion
import com.example.vicente_parrra.ui.components.Panel
import com.example.vicente_parrra.ui.components.Pantalla
import com.example.vicente_parrra.ui.components.moneda
import com.example.vicente_parrra.ui.theme.Espacio
import com.example.vicente_parrra.ui.theme.Musgo
import com.example.vicente_parrra.ui.theme.Pino

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(usuario: Usuario, indicadores: Indicadores?, ir: (Screen) -> Unit) {
    Pantalla("Panel de operación", "${usuario.nombre} · ${usuario.rol.etiqueta}") {
        if (usuario.puede("indicadores")) {
            if (indicadores == null) Text("Los indicadores aparecerán al cargar la operación.")
            else IndicadoresScreen(indicadores)
        }
        Text("Acciones", style = MaterialTheme.typography.titleLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Espacio.s),
            verticalArrangement = Arrangement.spacedBy(Espacio.s)
        ) {
            listOf(
                Screen.NuevoPedido, Screen.Pedidos, Screen.Clientes, Screen.Catalogo,
                Screen.Ventas, Screen.Rutas, Screen.Entregas, Screen.Usuarios
            ).filter { it.disponible(usuario) }.forEach { pantalla ->
                Accion(pantalla.titulo, { ir(pantalla) }, secundaria = pantalla != Screen.NuevoPedido)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IndicadoresScreen(datos: Indicadores) {
    val porcentaje = if (datos.meta > 0) datos.ventas * 100 / datos.meta else 0
    Panel(Modifier.fillMaxWidth()) {
        Text("Ventas registradas", style = MaterialTheme.typography.titleMedium)
        Text(moneda(datos.ventas), color = Pino, style = MaterialTheme.typography.displaySmall)
        Text("$porcentaje % de la meta de ${moneda(datos.meta)}", color = Musgo)
    }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Espacio.m),
        verticalArrangement = Arrangement.spacedBy(Espacio.m)
    ) {
        Metrica("Pedidos pendientes", datos.pendientes)
        Metrica("Pedidos en ruta", datos.enRuta)
        Metrica("Pedidos entregados", datos.entregados)
    }
    Panel(Modifier.fillMaxWidth()) {
        Text("Seguimiento de entregas", style = MaterialTheme.typography.titleLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Espacio.xl)) {
            Text("${datos.entregasRealizadas} realizadas")
            Text("${datos.entregasPendientes} por completar")
        }
    }
}

@Composable
private fun Metrica(etiqueta: String, valor: Int) {
    Panel(Modifier.widthIn(min = 140.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium)
        Text(valor.toString(), color = Pino, style = MaterialTheme.typography.headlineMedium)
    }
}
