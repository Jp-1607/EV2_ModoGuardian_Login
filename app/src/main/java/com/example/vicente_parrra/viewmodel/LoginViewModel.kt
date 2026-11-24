package com.example.vicente_parrra.viewmodel

import android.app.Application
import android.content.Context
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vicente_parrra.model.LoginErrores
import com.example.vicente_parrra.model.LoginUiState
import com.example.vicente_parrra.repository.AuthRepository
import com.example.vicente_parrra.repository.mensajeDe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository()
    private val preferencias = application.getSharedPreferences("insertcode", Context.MODE_PRIVATE)
    private val correoRecordado get() = preferencias.getString("ultimo_correo", null)
    private val datos = MutableStateFlow(
        LoginUiState(email = correoRecordado.orEmpty(), recordarCorreo = correoRecordado != null)
    )
    val estado = datos.asStateFlow()

    fun onEmailChange(valor: String) = datos.update {
        it.copy(email = valor, errores = it.errores.copy(email = null, general = null))
    }

    fun onPasswordChange(valor: String) = datos.update {
        it.copy(password = valor, errores = it.errores.copy(password = null, general = null))
    }

    fun recordarCorreo(valor: Boolean) = datos.update { it.copy(recordarCorreo = valor) }

    fun validarFormulario(): Boolean {
        val actual = datos.value
        val errores = LoginErrores(
            email = if (Patterns.EMAIL_ADDRESS.matcher(actual.email.trim()).matches()) null else "Ingresa un correo válido.",
            password = if (actual.password.length in 8..64) null else "Usa entre 8 y 64 caracteres."
        )
        datos.update { it.copy(errores = errores) }
        return errores == LoginErrores()
    }

    fun iniciarSesion() {
        if (datos.value.isLoading || !validarFormulario()) return
        val solicitud = datos.value
        datos.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val sesion = repository.autenticar(solicitud.email, solicitud.password)
                preferencias.edit().putString("ultimo_correo", sesion.usuario.correo.takeIf { solicitud.recordarCorreo }).apply()
                datos.update { it.copy(isLoading = false, password = "", sesion = sesion) }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                datos.update { it.copy(isLoading = false, errores = LoginErrores(general = mensajeDe(error))) }
            }
        }
    }

    fun cerrarSesion(aviso: String? = null) {
        val sesion = datos.value.sesion
        datos.value = LoginUiState(
            email = correoRecordado.orEmpty(),
            recordarCorreo = correoRecordado != null,
            errores = LoginErrores(general = aviso)
        )
        if (sesion != null) viewModelScope.launch {
            try {
                repository.cerrar(sesion)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                Unit
            }
        }
    }
}
