package com.example.vicente_parrra.repository

import com.example.vicente_parrra.model.Credenciales
import com.example.vicente_parrra.model.Sesion
import com.example.vicente_parrra.remote.ApiService
import com.example.vicente_parrra.remote.RetrofitInstance

class AuthRepository(private val api: ApiService = RetrofitInstance.api) {
    suspend fun autenticar(email: String, password: String): Sesion =
        api.autenticar(Credenciales(email.trim(), password))

    suspend fun cerrar(sesion: Sesion) {
        api.cerrar("Bearer ${sesion.token}")
    }
}
