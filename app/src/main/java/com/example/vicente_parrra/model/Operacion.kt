package com.example.vicente_parrra.model

enum class Rol(val etiqueta: String) {
    ADMIN("Administrador"),
    SUPERVISOR("Supervisor"),
    VENDEDOR("Vendedor"),
    REPARTIDOR("Repartidor")
}

enum class EstadoPedido(val etiqueta: String) {
    PENDIENTE("Pendiente"), EN_RUTA("En ruta"), ENTREGADO("Entregado");

    fun siguiente(): EstadoPedido? = when (this) {
        PENDIENTE -> EN_RUTA
        EN_RUTA -> ENTREGADO
        ENTREGADO -> null
    }
}

enum class Prioridad(val etiqueta: String) { NORMAL("Normal"), ALTA("Alta") }

data class Usuario(
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: Rol,
    val activo: Boolean,
    val permisos: List<String>
) {
    fun puede(permiso: String): Boolean = permiso in permisos
}

data class Sesion(val usuario: Usuario, val token: String)
data class Credenciales(val correo: String, val clave: String)
data class Cliente(val id: String, val nombre: String, val sucursal: String, val direccion: String)
data class Producto(val id: String, val nombre: String, val categoria: String, val descripcion: String, val precio: Long)
data class Venta(val id: String, val cliente: String, val fecha: String, val total: Long)
data class ItemPedido(val productoId: String, val nombre: String, val cantidad: Int, val precio: Long)
data class Pedido(
    val id: String,
    val clienteId: String,
    val cliente: String,
    val fecha: String,
    val prioridad: Prioridad,
    val estado: EstadoPedido,
    val total: Long,
    val items: List<ItemPedido>
)

data class Ruta(
    val id: String,
    val nombre: String,
    val sucursal: String,
    val vehiculo: String,
    val repartidor: String,
    val latitud: Double,
    val longitud: Double
)

data class Entrega(
    val id: String,
    val pedidoId: String,
    val rutaId: String,
    val orden: Int,
    val cliente: String,
    val direccion: String,
    val horario: String,
    val estado: EstadoPedido,
    val latitud: Double,
    val longitud: Double
)

data class Indicadores(
    val ventas: Long,
    val meta: Long,
    val pendientes: Int,
    val enRuta: Int,
    val entregados: Int,
    val entregasRealizadas: Int,
    val entregasPendientes: Int
)

data class Operacion(
    val clientes: List<Cliente>,
    val productos: List<Producto>,
    val pedidos: List<Pedido>,
    val ventas: List<Venta>,
    val rutas: List<Ruta>,
    val entregas: List<Entrega>,
    val indicadores: Indicadores?
)

data class RutaDisponible(val id: String, val nombre: String)
data class CantidadPedido(val productoId: String, val cantidad: Int)
data class NuevoPedido(
    val idOperacion: String,
    val clienteId: String,
    val rutaId: String,
    val fecha: String,
    val prioridad: Prioridad,
    val items: List<CantidadPedido>
)

data class CambioEntrega(val estado: EstadoPedido)
data class Administracion(val usuarios: List<Usuario>, val permisosPorRol: Map<String, List<String>>)
data class CambiosUsuario(
    val nombre: String,
    val correo: String,
    val rol: Rol,
    val activo: Boolean,
    val clave: String?,
    val permisos: List<String>
)
