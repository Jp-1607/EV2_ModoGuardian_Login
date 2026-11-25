package com.example.vicente_parrra.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.vicente_parrra.model.Entrega
import com.example.vicente_parrra.model.EstadoPedido
import com.example.vicente_parrra.model.Ruta
import com.example.vicente_parrra.ui.components.Accion
import com.example.vicente_parrra.ui.components.EstadoVisual
import com.example.vicente_parrra.ui.components.Panel
import com.example.vicente_parrra.ui.components.Pantalla
import com.example.vicente_parrra.ui.theme.Borde
import com.example.vicente_parrra.ui.theme.Cobre
import com.example.vicente_parrra.ui.theme.Marfil
import com.example.vicente_parrra.ui.theme.Musgo
import com.example.vicente_parrra.ui.theme.Ocre
import com.example.vicente_parrra.ui.theme.Papel
import com.example.vicente_parrra.ui.theme.Pino

@Composable
fun RutasScreen(rutas: List<Ruta>?, entregas: List<Entrega>, verEntregas: () -> Unit) {
    Pantalla("Rutas de despacho", "Coordenadas simuladas para la demostración.") {
        if (rutas.isNullOrEmpty()) Text("No hay rutas disponibles.")
        rutas.orEmpty().forEach { ruta ->
            val paradas = entregas.filter { it.rutaId == ruta.id }.sortedBy { it.orden }
            Panel(Modifier.fillMaxWidth()) {
                Text(ruta.nombre, style = MaterialTheme.typography.titleLarge)
                Text("${ruta.vehiculo} · ${ruta.sucursal}")
                Text("Repartidor: ${ruta.repartidor}")
                Mapa(ruta, paradas)
                paradas.forEach {
                    Text("${it.orden}. ${it.cliente} · ${it.horario}")
                    EstadoVisual(it.estado)
                }
                Accion("Ver entregas", verEntregas, secundaria = true)
            }
        }
    }
}

@Composable
fun EntregasScreen(entregas: List<Entrega>?, operativo: Boolean, cambiar: ((Entrega) -> Unit)?) {
    Pantalla("Entregas") {
        if (entregas.isNullOrEmpty()) Text("No hay entregas disponibles.")
        entregas.orEmpty().sortedBy { it.orden }.forEach { entrega ->
            Panel(Modifier.fillMaxWidth()) {
                Text("${entrega.id} · ${entrega.pedidoId}", style = MaterialTheme.typography.labelMedium)
                Text(entrega.cliente, style = MaterialTheme.typography.titleLarge)
                Text(entrega.direccion)
                Text("Horario: ${entrega.horario}")
                EstadoVisual(entrega.estado)
                val siguiente = entrega.estado.siguiente()
                if (cambiar != null && siguiente != null) {
                    Accion(
                        if (siguiente == EstadoPedido.EN_RUTA) "Iniciar despacho" else "Confirmar entrega",
                        { cambiar(entrega) }, habilitada = operativo
                    )
                }
            }
        }
    }
}

@Composable
private fun Mapa(ruta: Ruta, entregas: List<Entrega>) {
    val puntos = listOf(ruta.longitud to ruta.latitud) + entregas.map { it.longitud to it.latitud }
    val izquierda = puntos.minOf { it.first }
    val oesteEste = (puntos.maxOf { it.first } - izquierda).coerceAtLeast(0.002)
    val abajo = puntos.minOf { it.second }
    val surNorte = (puntos.maxOf { it.second } - abajo).coerceAtLeast(0.002)
    val descripcion = "Ruta simulada desde ${ruta.sucursal}. ${entregas.size} paradas en orden debajo del mapa."
    Canvas(
        Modifier.fillMaxWidth().heightIn(max = 280.dp).aspectRatio(1.9f)
            .background(Marfil, MaterialTheme.shapes.small).semantics { contentDescription = descripcion }
    ) {
        val borde = 20.dp.toPx()
        val ancho = (size.width - 2 * borde).coerceAtLeast(1f)
        val alto = (size.height - 2 * borde).coerceAtLeast(1f)
        val coordenadas = puntos.map { (x, y) ->
            Offset(borde + ancho * ((x - izquierda) / oesteEste).toFloat(),
                size.height - borde - alto * ((y - abajo) / surNorte).toFloat())
        }
        for (i in 1..7) {
            val x = size.width * i / 8
            val y = size.height * i / 8
            drawLine(Borde.copy(alpha = 0.22f), Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
            drawLine(Borde.copy(alpha = 0.22f), Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
        }
        val trazo = PathEffect.dashPathEffect(floatArrayOf(9.dp.toPx(), 5.dp.toPx()))
        coordenadas.zipWithNext().forEach { (origen, destino) ->
            drawLine(Pino, origen, destino, 2.dp.toPx(), pathEffect = trazo)
        }
        coordenadas.forEachIndexed { indice, punto ->
            val color = when (entregas.getOrNull(indice - 1)?.estado) {
                EstadoPedido.PENDIENTE -> Ocre
                EstadoPedido.EN_RUTA -> Cobre
                EstadoPedido.ENTREGADO -> Musgo
                null -> Pino
            }
            drawCircle(Papel, 9.dp.toPx(), punto)
            drawCircle(color, 6.dp.toPx(), punto)
        }
    }
}
