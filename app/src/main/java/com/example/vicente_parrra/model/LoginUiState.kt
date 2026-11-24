package com.example.vicente_parrra.model

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val recordarCorreo: Boolean = false,
    val sesion: Sesion? = null,
    val errores: LoginErrores = LoginErrores()
) {
    val isLoginEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}
