package com.example.vicente_parrra.repository

import com.example.vicente_parrra.local.DistribucionDao
import com.example.vicente_parrra.local.OperacionLocal
import com.example.vicente_parrra.model.CambioEntrega
import com.example.vicente_parrra.model.CambiosUsuario
import com.example.vicente_parrra.model.EstadoPedido
import com.example.vicente_parrra.model.NuevoPedido
import com.example.vicente_parrra.model.Operacion
import com.example.vicente_parrra.model.Sesion
import com.example.vicente_parrra.remote.ApiService
import com.example.vicente_parrra.remote.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import java.io.IOException
import retrofit2.HttpException

class DistribucionRepository(
    private val dao: DistribucionDao,
    private val api: ApiService = RetrofitInstance.api,
    private val gson: Gson = Gson()
) {
    private fun Sesion.autorizacion() = "Bearer $token"
    private fun Sesion.cuenta() = "${usuario.id}:${usuario.rol.name}:${usuario.permisos.sorted().joinToString(",")}"

    suspend fun local(sesion: Sesion): Pair<Operacion, Long>? {
        val registro = dao.consultar(sesion.cuenta()) ?: return null
        val operacion = try {
            gson.fromJson(registro.contenido, Operacion::class.java)
        } catch (_: JsonParseException) {
            null
        }
        if (operacion == null) {
            dao.borrar(sesion.cuenta())
            return null
        }
        return operacion to registro.actualizada
    }

    suspend fun actualizar(sesion: Sesion): Pair<Operacion, Long> {
        val operacion = api.consultar(sesion.autorizacion())
        val ahora = System.currentTimeMillis()
        dao.guardar(OperacionLocal(sesion.cuenta(), gson.toJson(operacion), ahora))
        return operacion to ahora
    }

    suspend fun invalidar(sesion: Sesion) = dao.borrar(sesion.cuenta())
    suspend fun rutasDisponibles(sesion: Sesion) = api.rutasDisponibles(sesion.autorizacion())
    suspend fun crearPedido(sesion: Sesion, pedido: NuevoPedido) = api.crearPedido(sesion.autorizacion(), pedido)
    suspend fun actualizarEntrega(sesion: Sesion, id: String, estado: EstadoPedido) =
        api.actualizarEntrega(sesion.autorizacion(), id, CambioEntrega(estado))
    suspend fun usuarios(sesion: Sesion) = api.usuarios(sesion.autorizacion())
    suspend fun guardarUsuario(sesion: Sesion, id: String?, cambios: CambiosUsuario) =
        if (id == null) api.crearUsuario(sesion.autorizacion(), cambios)
        else api.editarUsuario(sesion.autorizacion(), id, cambios)
}

fun mensajeDe(error: Exception): String = when (error) {
    is HttpException -> runCatching {
        val cuerpo = error.response()?.errorBody()?.string()
        cuerpo?.let { JsonParser.parseString(it).asJsonObject.get("mensaje")?.asString }
    }.getOrNull() ?: "La API no pudo completar la operación."
    is IOException -> "No hay conexión con la API de prueba. Revisa la conexión y vuelve a intentar."
    else -> "Ocurrió un error al cargar o guardar la información."
}
