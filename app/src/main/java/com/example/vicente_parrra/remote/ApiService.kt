package com.example.vicente_parrra.remote

import com.example.vicente_parrra.model.Administracion
import com.example.vicente_parrra.model.CambioEntrega
import com.example.vicente_parrra.model.CambiosUsuario
import com.example.vicente_parrra.model.Credenciales
import com.example.vicente_parrra.model.Entrega
import com.example.vicente_parrra.model.NuevoPedido
import com.example.vicente_parrra.model.Operacion
import com.example.vicente_parrra.model.Pedido
import com.example.vicente_parrra.model.RutaDisponible
import com.example.vicente_parrra.model.Sesion
import com.example.vicente_parrra.model.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("sesiones")
    suspend fun autenticar(@Body datos: Credenciales): Sesion

    @DELETE("sesiones/actual")
    suspend fun cerrar(@Header("Authorization") sesion: String): Response<Unit>

    @GET("operacion")
    suspend fun consultar(@Header("Authorization") sesion: String): Operacion

    @GET("rutas-disponibles")
    suspend fun rutasDisponibles(@Header("Authorization") sesion: String): List<RutaDisponible>

    @POST("pedidos")
    suspend fun crearPedido(@Header("Authorization") sesion: String, @Body datos: NuevoPedido): Pedido

    @PATCH("entregas/{id}/estado")
    suspend fun actualizarEntrega(
        @Header("Authorization") sesion: String,
        @Path("id") id: String,
        @Body datos: CambioEntrega
    ): Entrega

    @GET("usuarios")
    suspend fun usuarios(@Header("Authorization") sesion: String): Administracion

    @POST("usuarios")
    suspend fun crearUsuario(@Header("Authorization") sesion: String, @Body datos: CambiosUsuario): Usuario

    @PUT("usuarios/{id}")
    suspend fun editarUsuario(
        @Header("Authorization") sesion: String,
        @Path("id") id: String,
        @Body datos: CambiosUsuario
    ): Usuario
}
