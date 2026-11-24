package com.example.vicente_parrra.navigation

import com.example.vicente_parrra.model.Usuario

enum class Screen(val route: String, val titulo: String, val permiso: String? = null) {
    Inicio("inicio", "Inicio"),
    Pedidos("pedidos", "Pedidos", "pedidos"),
    Catalogo("catalogo", "Catálogo", "comercial"),
    Clientes("clientes", "Clientes", "comercial"),
    Ventas("ventas", "Ventas", "comercial"),
    Rutas("rutas", "Rutas", "despacho"),
    Entregas("entregas", "Entregas", "despacho"),
    Perfil("perfil", "Perfil"),
    Usuarios("usuarios", "Usuarios", "usuarios"),
    NuevoPedido("nuevo_pedido", "Nuevo pedido", "crearPedido");

    fun disponible(usuario: Usuario): Boolean = permiso == null || usuario.puede(permiso)

    companion object {
        fun menu(usuario: Usuario): List<Screen> =
            listOf(Inicio, Pedidos, Catalogo, Rutas, Perfil).filter { it.disponible(usuario) }
    }
}
