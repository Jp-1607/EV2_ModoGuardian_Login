package com.example.vicente_parrra.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vicente_parrra.model.LoginErrores
import com.example.vicente_parrra.model.LoginUiState
import com.example.vicente_parrra.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()
    private val preferencias = application.getSharedPreferences("guardian", Context.MODE_PRIVATE)

    // Recupera solo el último correo; la contraseña nunca se guarda localmente.
    private val _estado = MutableStateFlow(
        LoginUiState(
            email = preferencias.getString("ultimo_correo", "") ?: ""
        )
    )

    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun onEmailChange(valor: String) {
        _estado.update {
            it.copy(
                email = valor,
                errores = it.errores.copy(email = null, general = null)
            )
        }
    }

    fun onPasswordChange(valor: String) {
        _estado.update {
            it.copy(
                password = valor,
                errores = it.errores.copy(password = null, general = null)
            )
        }
    }

    fun validarFormulario(): Boolean {
        val email = _estado.value.email
        val password = _estado.value.password

        // Las validaciones se calculan juntas para mostrar todos los errores del formulario.
        val errorEmail = when {
            email.isBlank() -> "Ingrese un correo"
            !email.contains("@") -> "Correo inválido"
            else -> null
        }

        val errorPassword = when {
            password.isBlank() -> "Ingrese una contraseña"
            password.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }

        _estado.update {
            it.copy(
                errores = LoginErrores(
                    email = errorEmail,
                    password = errorPassword
                )
            )
        }

        return errorEmail == null && errorPassword == null
    }

    fun iniciarSesion(onSuccess: (String) -> Unit) {
        if (!validarFormulario()) return

        _estado.update {
            it.copy(
                isLoading = true,
                errores = LoginErrores()
            )
        }

        viewModelScope.launch {
            try {
                val resultado = repository.autenticar(
                    _estado.value.email,
                    _estado.value.password
                )

                resultado.onSuccess { usuario ->
                    preferencias.edit()
                        .putString("ultimo_correo", usuario.email)
                        .apply()

                    _estado.update {
                        it.copy(
                            isLoading = false,
                            usuarioLogueado = usuario
                        )
                    }

                    onSuccess(usuario.rol)
                }

                resultado.onFailure { error ->
                    _estado.update {
                        it.copy(
                            isLoading = false,
                            errores = LoginErrores(
                                general = error.message ?: "Error al iniciar sesión"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _estado.update {
                    it.copy(
                        isLoading = false,
                        errores = LoginErrores(
                            general = "Error de conexión"
                        )
                    )
                }
            }
        }
    }

    fun cerrarSesion() {
        val correo = preferencias.getString("ultimo_correo", "") ?: ""
        _estado.value = LoginUiState(email = correo)
    }
}

