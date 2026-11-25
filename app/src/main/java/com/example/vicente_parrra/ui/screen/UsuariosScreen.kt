package com.example.vicente_parrra.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.vicente_parrra.model.CambiosUsuario
import com.example.vicente_parrra.model.Rol
import com.example.vicente_parrra.model.Usuario
import com.example.vicente_parrra.ui.components.Accion
import com.example.vicente_parrra.ui.components.Opcion
import com.example.vicente_parrra.ui.components.Panel
import com.example.vicente_parrra.ui.components.Pantalla
import com.example.vicente_parrra.ui.theme.Espacio
import com.example.vicente_parrra.viewmodel.OperacionUiState

@Composable
fun UsuariosScreen(
    estado: OperacionUiState,
    guardar: (String?, CambiosUsuario) -> Unit,
    limpiar: () -> Unit
) {
    var editando by rememberSaveable { mutableStateOf<String?>(null) }
    val usuarios = estado.usuarios
    val disponible = !estado.cargando && !estado.guardando && !estado.sinConexion && usuarios != null

    LaunchedEffect(estado.usuarioGuardado) {
        if (estado.usuarioGuardado) editando = null
    }

    Pantalla("Usuarios de prueba", "Asigna roles y revisa los permisos de cada cuenta.") {
        val seleccionado = editando
        if (seleccionado == null) {
            Accion("Nueva cuenta", { limpiar(); editando = "nueva" }, habilitada = disponible)
            if (usuarios.isNullOrEmpty()) Text("Todavía no hay usuarios disponibles.")
            usuarios.orEmpty().sortedBy { it.nombre }.forEach { usuario ->
                Panel(Modifier.fillMaxWidth()) {
                    Text(usuario.nombre, style = MaterialTheme.typography.titleLarge)
                    Text(usuario.correo)
                    Text("${usuario.rol.etiqueta} · ${if (usuario.activo) "Activa" else "Inactiva"}")
                    Accion("Editar cuenta", { limpiar(); editando = usuario.id },
                        habilitada = disponible, secundaria = true)
                }
            }
        } else {
            val usuario = usuarios?.find { it.id == seleccionado }
            if (seleccionado == "nueva" || usuario != null) {
                FormularioUsuario(usuario, estado.permisosPorRol, estado.guardando, disponible,
                    { guardar(usuario?.id, it) }, { limpiar(); editando = null })
            }
        }
    }
}

@Composable
private fun FormularioUsuario(
    usuario: Usuario?,
    permisosPorRol: Map<String, List<String>>,
    guardando: Boolean,
    disponible: Boolean,
    guardar: (CambiosUsuario) -> Unit,
    volver: () -> Unit
) {
    var nombre by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.nombre.orEmpty()) }
    var correo by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.correo.orEmpty()) }
    var rol by rememberSaveable(usuario?.id) { mutableStateOf((usuario?.rol ?: Rol.VENDEDOR).name) }
    var activo by rememberSaveable(usuario?.id) { mutableStateOf(usuario?.activo ?: true) }
    var clave by remember(usuario?.id) { mutableStateOf("") }
    var error by rememberSaveable(usuario?.id) { mutableStateOf<String?>(null) }
    val rolElegido = Rol.valueOf(rol)
    var permisos by rememberSaveable(usuario?.id) {
        mutableStateOf(ArrayList(usuario?.permisos ?: permisosPorRol[rol].orEmpty()))
    }

    Text(if (usuario == null) "Nueva cuenta" else "Editar cuenta", style = MaterialTheme.typography.titleLarge)
    OutlinedTextField(nombre, { nombre = it.take(80) }, Modifier.fillMaxWidth(),
        enabled = !guardando, label = { Text("Nombre ficticio") }, singleLine = true)
    OutlinedTextField(correo, { correo = it.take(76) }, Modifier.fillMaxWidth(),
        enabled = !guardando, label = { Text("Correo @insertcode.test") }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
    OutlinedTextField(clave, { clave = it.take(64) }, Modifier.fillMaxWidth(),
        enabled = !guardando, label = { Text(if (usuario == null) "Contraseña de prueba" else "Nueva contraseña · opcional") },
        singleLine = true, visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
    Selector("Rol", rol, Rol.entries.map { it.name to it.etiqueta }, !guardando) {
        rol = it
        permisos = ArrayList(permisosPorRol[it].orEmpty())
    }
    Opcion("Cuenta activa", activo, !guardando) { activo = it }
    Panel(Modifier.fillMaxWidth()) {
        Text("Permisos del rol", style = MaterialTheme.typography.titleMedium)
        permisosPorRol[rol].orEmpty().forEach { permiso ->
            Opcion(etiquetaPermiso(permiso), permiso in permisos, !guardando) { habilitar ->
                permisos = ArrayList(if (habilitar) permisos + permiso else permisos - permiso)
            }
        }
    }
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    Row(horizontalArrangement = Arrangement.spacedBy(Espacio.s)) {
        Accion(if (guardando) "Guardando…" else "Guardar", {
            error = when {
                nombre.trim().length < 3 -> "Ingresa un nombre de al menos 3 caracteres."
                !Regex("[a-zA-Z0-9._+-]{1,60}@insertcode\\.test", RegexOption.IGNORE_CASE).matches(correo.trim()) ->
                    "Usa un correo ficticio @insertcode.test."
                (usuario == null || clave.isNotEmpty()) && clave.length < 8 -> "Usa una contraseña de al menos 8 caracteres."
                "crearPedido" in permisos && !permisos.containsAll(listOf("comercial", "pedidos")) ->
                    "Para registrar pedidos, habilita el acceso comercial y la consulta de pedidos."
                "actualizarEntrega" in permisos && "despacho" !in permisos ->
                    "Para actualizar entregas, habilita la consulta de rutas y entregas."
                else -> {
                    guardar(CambiosUsuario(nombre.trim(), correo.trim(), rolElegido, activo, clave.ifEmpty { null }, permisos))
                    null
                }
            }
        }, habilitada = disponible)
        Accion("Cancelar", volver, habilitada = !guardando, secundaria = true)
    }
}
