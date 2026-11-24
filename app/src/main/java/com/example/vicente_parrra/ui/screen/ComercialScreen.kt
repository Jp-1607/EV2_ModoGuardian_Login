package com.example.vicente_parrra.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.vicente_parrra.model.CantidadPedido
import com.example.vicente_parrra.model.Cliente
import com.example.vicente_parrra.model.NuevoPedido
import com.example.vicente_parrra.model.Pedido
import com.example.vicente_parrra.model.Prioridad
import com.example.vicente_parrra.model.Producto
import com.example.vicente_parrra.model.Venta
import com.example.vicente_parrra.ui.components.Accion
import com.example.vicente_parrra.ui.components.EstadoVisual
import com.example.vicente_parrra.ui.components.Panel
import com.example.vicente_parrra.ui.components.Pantalla
import com.example.vicente_parrra.ui.components.moneda
import com.example.vicente_parrra.ui.theme.Espacio
import com.example.vicente_parrra.ui.theme.Musgo
import com.example.vicente_parrra.viewmodel.OperacionUiState
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun ClientesScreen(clientes: List<Cliente>?) {
    Pantalla("Clientes") {
        TextoLista(clientes, "No hay clientes.")
        clientes.orEmpty().forEach { cliente ->
            Panel(Modifier.fillMaxWidth()) {
                Text(cliente.nombre, style = MaterialTheme.typography.titleLarge)
                Text("${cliente.id} · Sucursal ${cliente.sucursal}")
                Text(cliente.direccion)
            }
        }
    }
}

@Composable
fun CatalogoScreen(productos: List<Producto>?, cotizar: ((String) -> Unit)?) {
    Pantalla("Catálogo", "Soluciones de software y hardware.") {
        TextoLista(productos, "No hay productos en el catálogo.")
        productos.orEmpty().forEach { producto ->
            Panel(Modifier.fillMaxWidth()) {
                Text("${producto.categoria} · ${producto.id}", style = MaterialTheme.typography.labelMedium)
                Text(producto.nombre, style = MaterialTheme.typography.titleLarge)
                Text(producto.descripcion)
                Text(moneda(producto.precio), color = Musgo, style = MaterialTheme.typography.headlineMedium)
                cotizar?.let { Accion("Usar en pedido", { it(producto.id) }, secundaria = true) }
            }
        }
    }
}

@Composable
fun VentasScreen(ventas: List<Venta>?) {
    Pantalla("Ventas", "Pedidos con entrega confirmada.") {
        TextoLista(ventas, "Todavía no hay ventas registradas.")
        ventas.orEmpty().forEach { venta ->
            Panel(Modifier.fillMaxWidth()) {
                Text(venta.cliente, style = MaterialTheme.typography.titleLarge)
                Text("${venta.id} · ${venta.fecha}")
                Text(moneda(venta.total), color = Musgo, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PedidosScreen(pedidos: List<Pedido>?, nuevo: (() -> Unit)?) {
    Pantalla("Pedidos", "Consulta los productos y el estado de cada pedido.") {
        nuevo?.let { Accion("Nuevo pedido", it) }
        TextoLista(pedidos, "Todavía no hay pedidos.")
        pedidos.orEmpty().asReversed().forEach { pedido ->
            Panel(Modifier.fillMaxWidth()) {
                Text(pedido.id, style = MaterialTheme.typography.labelMedium)
                Text(pedido.cliente, style = MaterialTheme.typography.titleLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Espacio.s), verticalArrangement = Arrangement.spacedBy(Espacio.xs)) {
                    EstadoVisual(pedido.estado)
                    Text("${pedido.fecha} · Prioridad ${pedido.prioridad.etiqueta.lowercase()}")
                }
                HorizontalDivider()
                pedido.items.forEach { Text("${it.cantidad} × ${it.nombre}") }
                Text(moneda(pedido.total), color = Musgo, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun <T> TextoLista(datos: List<T>?, vacia: String) {
    if (datos == null) Text("Todavía no hay información disponible.")
    else if (datos.isEmpty()) Text(vacia)
}

@Composable
fun NuevoPedidoScreen(
    estado: OperacionUiState,
    productoInicial: String?,
    guardar: (NuevoPedido) -> Unit,
    verPedidos: () -> Unit
) {
    val clientes = estado.operacion?.clientes.orEmpty()
    val productos = estado.operacion?.productos.orEmpty()
    val rutas = estado.rutasDisponibles.orEmpty()
    var cliente by rememberSaveable { mutableStateOf("") }
    var ruta by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf(formatoFecha().format(Date())) }
    var prioridad by rememberSaveable { mutableStateOf(Prioridad.NORMAL.name) }
    var producto by rememberSaveable { mutableStateOf(productoInicial.orEmpty()) }
    var cantidad by rememberSaveable { mutableStateOf(if (productoInicial == null) "" else "1") }
    var idsProductos by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    var cantidades by rememberSaveable { mutableStateOf(arrayListOf<Int>()) }
    val operacion = rememberSaveable { UUID.randomUUID().toString() }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val enviando = estado.guardando
    val guardado = estado.pedidoGuardado

    LaunchedEffect(rutas) {
        if (ruta.isBlank() && rutas.size == 1) ruta = rutas.first().id
    }

    Pantalla("Nuevo pedido") {
        if (guardado != null) {
            Panel(Modifier.fillMaxWidth()) {
                Text("Pedido $guardado registrado.", style = MaterialTheme.typography.titleLarge)
                Accion("Ver pedidos", verPedidos)
            }
        } else {
            Selector("Cliente", cliente, clientes.map { it.id to it.nombre }, !enviando) { cliente = it }
            Selector("Ruta de despacho", ruta, rutas.map { it.id to it.nombre }, !enviando) { ruta = it }
            OutlinedTextField(fecha, { fecha = it.take(10) }, Modifier.fillMaxWidth(), enabled = !enviando,
                label = { Text("Fecha del pedido · AAAA-MM-DD") }, singleLine = true)
            Selector("Prioridad", prioridad, Prioridad.entries.map { it.name to it.etiqueta }, !enviando) { prioridad = it }
            Panel(Modifier.fillMaxWidth()) {
                Text("Productos del pedido", style = MaterialTheme.typography.titleLarge)
                Selector("Producto", producto, productos.map { it.id to it.nombre }, !enviando) { producto = it }
                OutlinedTextField(cantidad, { cantidad = it.filter(Char::isDigit).take(2) }, Modifier.fillMaxWidth(),
                    enabled = !enviando, label = { Text("Cantidad · 1 a 99") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Accion("Agregar producto", {
                    val unidades = cantidad.toIntOrNull()
                    error = when {
                        producto.isBlank() -> "Selecciona un producto."
                        unidades == null || unidades !in 1..99 -> "Ingresa una cantidad de 1 a 99."
                        producto in idsProductos -> "El producto ya está en el pedido."
                        else -> {
                            idsProductos = ArrayList(idsProductos + producto)
                            cantidades = ArrayList(cantidades + unidades)
                            producto = ""
                            cantidad = ""
                            null
                        }
                    }
                }, habilitada = !enviando, secundaria = true)
                idsProductos.forEachIndexed { indice, id ->
                    val nombre = productos.find { it.id == id }?.nombre ?: id
                    Text("${cantidades[indice]} × $nombre")
                    Accion("Quitar $nombre", {
                        idsProductos = ArrayList(idsProductos.filterIndexed { i, _ -> i != indice })
                        cantidades = ArrayList(cantidades.filterIndexed { i, _ -> i != indice })
                    }, habilitada = !enviando, secundaria = true)
                }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Accion(if (enviando) "Registrando…" else "Registrar pedido", {
                val items = idsProductos.zip(cantidades).map { (id, unidades) -> CantidadPedido(id, unidades) }
                val unidades = cantidad.toIntOrNull()
                val pendientes = if (producto.isNotBlank() && unidades != null && unidades in 1..99 && producto !in idsProductos) {
                    items + CantidadPedido(producto, unidades)
                } else items
                error = when {
                    cliente.isBlank() || ruta.isBlank() -> "Selecciona un cliente y una ruta."
                    !fechaValida(fecha) -> "Ingresa una fecha válida en formato AAAA-MM-DD."
                    producto.isNotBlank() && pendientes.size == items.size -> "Revisa el producto seleccionado y su cantidad."
                    pendientes.isEmpty() -> "Agrega al menos un producto."
                    else -> {
                        guardar(NuevoPedido(operacion, cliente, ruta, fecha, Prioridad.valueOf(prioridad), pendientes))
                        null
                    }
                }
            }, Modifier.fillMaxWidth(), habilitada = !enviando && !estado.cargando && !estado.sinConexion)
        }
    }
}

@Composable
fun Selector(
    etiqueta: String,
    seleccion: String,
    opciones: List<Pair<String, String>>,
    habilitado: Boolean = true,
    elegir: (String) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    val nombre = opciones.firstOrNull { it.first == seleccion }?.second
    androidx.compose.foundation.layout.Box {
        Accion(if (nombre == null) "Seleccionar ${etiqueta.lowercase()}" else "$etiqueta · $nombre",
            { abierto = true }, Modifier.fillMaxWidth(), habilitado && opciones.isNotEmpty(), secundaria = true)
        DropdownMenu(abierto, { abierto = false }) {
            opciones.forEach { (id, texto) ->
                DropdownMenuItem(text = { Text(texto) }, onClick = { elegir(id); abierto = false })
            }
        }
    }
}

private fun formatoFecha() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false }

internal fun fechaValida(fecha: String): Boolean {
    if (!Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(fecha)) return false
    val posicion = ParsePosition(0)
    return formatoFecha().parse(fecha, posicion) != null && posicion.index == fecha.length
}
