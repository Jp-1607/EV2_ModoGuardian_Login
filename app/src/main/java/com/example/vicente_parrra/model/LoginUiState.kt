package com.example.vicente_parrra.model

data class LoginUiState(
    val registrando: Boolean = false,
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val recordarCorreo: Boolean = false,
    val sesion: Sesion? = null,
    val errores: LoginErrores = LoginErrores()
)

data class RegistroCuenta(val nombre: String, val correo: String, val clave: String)
data class DesafioGoogle(val modo: String, val nonce: String, val clienteId: String?)
data class CredencialGoogle(val nonce: String, val token: String? = null, val cuentaDemo: String? = null)
