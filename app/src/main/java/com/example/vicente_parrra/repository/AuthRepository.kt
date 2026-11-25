package com.example.vicente_parrra.repository

import com.example.vicente_parrra.model.CredencialGoogle
import com.example.vicente_parrra.model.Credenciales
import com.example.vicente_parrra.model.RegistroCuenta
import com.example.vicente_parrra.model.Sesion
import com.example.vicente_parrra.remote.ApiService
import com.example.vicente_parrra.remote.RetrofitInstance

class AuthRepository(private val api: ApiService = RetrofitInstance.api) {
    suspend fun autenticar(email: String, password: String): Sesion =
        api.autenticar(Credenciales(email.trim(), password))

    suspend fun registrar(nombre: String, email: String, password: String): Sesion =
        api.registrar(RegistroCuenta(nombre.trim(), email.trim(), password))

    suspend fun desafioGoogle() = api.desafioGoogle()

    suspend fun autenticarGoogle(credencial: CredencialGoogle, registro: Boolean): Sesion =
        if (registro) api.registrarGoogle(credencial) else api.ingresarGoogle(credencial)

    suspend fun cerrar(sesion: Sesion) {
        api.cerrar("Bearer ${sesion.token}")
    }
}
