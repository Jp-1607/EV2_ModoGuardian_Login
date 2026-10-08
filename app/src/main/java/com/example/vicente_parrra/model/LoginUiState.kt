package com.example.vicente_parrra.model

data class UsuarioGuardian(
    val email: String,
    val rol: String,
    val nombre: String
)

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val usuarioLogueado: UsuarioGuardian? = null,
    val errores: LoginErrores = LoginErrores()
) {
    val isLoginEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}
