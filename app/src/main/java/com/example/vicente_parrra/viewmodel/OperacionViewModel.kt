package com.example.vicente_parrra.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vicente_parrra.local.AppDatabase
import com.example.vicente_parrra.model.CambiosUsuario
import com.example.vicente_parrra.model.Entrega
import com.example.vicente_parrra.model.NuevoPedido
import com.example.vicente_parrra.model.Operacion
import com.example.vicente_parrra.model.RutaDisponible
import com.example.vicente_parrra.model.Sesion
import com.example.vicente_parrra.model.Usuario
import com.example.vicente_parrra.repository.DistribucionRepository
import com.example.vicente_parrra.repository.mensajeDe
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class OperacionUiState(
    val operacion: Operacion? = null,
    val actualizada: Long? = null,
    val sinConexion: Boolean = false,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val aviso: String? = null,
    val sesionVencida: Boolean = false,
    val rutasDisponibles: List<RutaDisponible>? = null,
    val usuarios: List<Usuario>? = null,
    val permisosPorRol: Map<String, List<String>> = emptyMap(),
    val pedidoGuardado: String? = null,
    val usuarioGuardado: Boolean = false
)

class OperacionViewModel(application: Application) : AndroidViewModel(application) {
    private val repositorio = DistribucionRepository(AppDatabase.obtener(application).distribucion())
    private val datos = MutableStateFlow(OperacionUiState())
    val estado = datos.asStateFlow()
    private var sesion: Sesion? = null
    private var tarea: Job? = null

    fun conectar(actual: Sesion?) {
        if (sesion?.token == actual?.token) return
        tarea?.cancel()
        sesion = actual
        datos.value = OperacionUiState()
        if (actual != null) ejecutar {
            repositorio.local(it)?.let { copia -> mostrar(copia, sinConexion = true) }
            refrescar(it)
        }
    }

    fun actualizar() = ejecutar { refrescar(it) }

    fun crearPedido(solicitud: NuevoPedido) = ejecutar(guardar = true) { actual ->
        val pedido = repositorio.crearPedido(actual, solicitud)
        datos.update { it.copy(pedidoGuardado = pedido.id, aviso = "Pedido ${pedido.id} registrado.") }
        refrescar(actual)
    }

    fun cambiarEntrega(entrega: Entrega) {
        val siguiente = entrega.estado.siguiente() ?: return
        ejecutar(guardar = true) { actual ->
            repositorio.actualizarEntrega(actual, entrega.id, siguiente)
            datos.update { it.copy(aviso = "Entrega ${entrega.id} actualizada.") }
            refrescar(actual)
        }
    }

    fun guardarUsuario(id: String?, cambios: CambiosUsuario) = ejecutar(guardar = true) { actual ->
        val usuario = repositorio.guardarUsuario(actual, id, cambios)
        datos.update { anterior ->
            val usuarios = anterior.usuarios.orEmpty().filterNot { it.id == usuario.id } + usuario
            anterior.copy(usuarios = usuarios, usuarioGuardado = true, aviso = "Usuario guardado.")
        }
    }

    fun limpiarFormulario() = datos.update {
        it.copy(pedidoGuardado = null, usuarioGuardado = false, error = null)
    }

    private suspend fun refrescar(actual: Sesion) {
        mostrar(repositorio.actualizar(actual), sinConexion = false)
        if (actual.usuario.puede("crearPedido")) {
            val rutas = repositorio.rutasDisponibles(actual)
            datos.update { it.copy(rutasDisponibles = rutas) }
        }
        if (actual.usuario.puede("usuarios")) {
            val administracion = repositorio.usuarios(actual)
            datos.update { it.copy(usuarios = administracion.usuarios, permisosPorRol = administracion.permisosPorRol) }
        }
    }

    private fun mostrar(copia: Pair<Operacion, Long>, sinConexion: Boolean) = datos.update {
        it.copy(operacion = copia.first, actualizada = copia.second, sinConexion = sinConexion)
    }

    private fun ejecutar(guardar: Boolean = false, accion: suspend (Sesion) -> Unit) {
        val actual = sesion ?: return
        if (tarea?.isActive == true) return
        datos.update { it.copy(cargando = !guardar, guardando = guardar, error = null, aviso = null) }
        tarea = viewModelScope.launch {
            try {
                accion(actual)
                datos.update { it.copy(sinConexion = false) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                if (error is HttpException && error.code() == 401) {
                    datos.value = OperacionUiState(sesionVencida = true)
                    try {
                        repositorio.invalidar(actual)
                    } catch (cancelado: CancellationException) {
                        throw cancelado
                    } catch (_: Exception) {
                        Unit
                    }
                } else {
                    datos.update { it.copy(error = mensajeDe(error), sinConexion = error is IOException) }
                }
            } finally {
                if (sesion?.token == actual.token) datos.update { it.copy(cargando = false, guardando = false) }
            }
        }
    }
}
