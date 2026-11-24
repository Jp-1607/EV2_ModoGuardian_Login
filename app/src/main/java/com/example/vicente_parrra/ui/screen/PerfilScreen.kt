package com.example.vicente_parrra.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.vicente_parrra.model.Usuario
import com.example.vicente_parrra.ui.components.Accion
import com.example.vicente_parrra.ui.components.Panel
import com.example.vicente_parrra.ui.components.Pantalla

@Composable
fun PerfilScreen(usuario: Usuario, salir: () -> Unit) {
    Pantalla("Perfil") {
        Panel(Modifier.fillMaxWidth()) {
            Text(usuario.nombre, style = MaterialTheme.typography.headlineMedium)
            Text(usuario.rol.etiqueta, style = MaterialTheme.typography.titleMedium)
            Text(usuario.correo)
        }
        Panel(Modifier.fillMaxWidth()) {
            Text("Permisos de la cuenta", style = MaterialTheme.typography.titleLarge)
            usuario.permisos.forEach { Text(etiquetaPermiso(it)) }
        }
        Accion("Cerrar sesión", salir, secundaria = true)
    }
}

internal fun etiquetaPermiso(permiso: String): String = when (permiso) {
    "usuarios" -> "Gestionar usuarios de prueba y sus roles"
    "comercial" -> "Consultar clientes, catálogo y ventas"
    "pedidos" -> "Consultar pedidos"
    "despacho" -> "Consultar rutas y entregas"
    "indicadores" -> "Consultar indicadores"
    "crearPedido" -> "Registrar pedidos"
    "actualizarEntrega" -> "Actualizar entregas asignadas"
    else -> permiso
}
