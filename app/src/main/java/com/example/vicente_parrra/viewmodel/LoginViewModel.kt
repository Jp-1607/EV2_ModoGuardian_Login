package com.example.vicente_parrra.viewmodel

import android.app.Application
import android.content.Context
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.vicente_parrra.model.CredencialGoogle
import com.example.vicente_parrra.model.DesafioGoogle
import com.example.vicente_parrra.model.LoginErrores
import com.example.vicente_parrra.model.LoginUiState
import com.example.vicente_parrra.model.Sesion
import com.example.vicente_parrra.repository.AuthRepository
import com.example.vicente_parrra.repository.mensajeDe
import com.example.vicente_parrra.ui.utils.ErrorCuentaGoogle
import com.example.vicente_parrra.ui.utils.GoogleCuentaAndroid
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

    fun onNombreChange(valor: String) = datos.update {
        it.copy(nombre = valor, errores = it.errores.copy(nombre = null, general = null))
    }

    fun onEmailChange(valor: String) = datos.update {
        it.copy(email = valor, errores = it.errores.copy(email = null, general = null))
    }

    fun onPasswordChange(valor: String) = datos.update {
        it.copy(password = valor, errores = it.errores.copy(password = null, general = null))
    }

    fun recordarCorreo(valor: Boolean) = datos.update { it.copy(recordarCorreo = valor) }

    fun cambiarModo() = datos.update {
        if (it.isLoading) it else it.copy(registrando = !it.registrando, nombre = "", password = "", errores = LoginErrores())
    }

    fun validarFormulario(): Boolean {
        val actual = datos.value
        val correo = actual.email.trim()
        val errores = LoginErrores(
            nombre = if (actual.registrando && actual.nombre.trim().length !in 3..80) "Ingresa un nombre ficticio de 3 a 80 caracteres." else null,
            email = when {
                correo.isEmpty() -> "Ingresa tu correo."
                !Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> "Ingresa un correo válido."
                actual.registrando && !correo.endsWith("@insertcode.test", ignoreCase = true) -> "Usa un correo ficticio @insertcode.test."
                else -> null
            },
            password = when {
                actual.password.isEmpty() -> "Ingresa tu contraseña."
                actual.password.length !in 8..64 -> "Usa entre 8 y 64 caracteres."
                else -> null
            }
        )
        datos.update { it.copy(errores = errores) }
        return errores == LoginErrores()
    }

    fun iniciarSesion() {
        if (datos.value.isLoading || !validarFormulario()) return
        val solicitud = datos.value
        viewModelScope.launch {
            procesar {
                val sesion = if (solicitud.registrando) {
                    repository.registrar(solicitud.nombre, solicitud.email, solicitud.password)
                } else {
                    repository.autenticar(solicitud.email, solicitud.password)
                }
                preferencias.edit().putString("ultimo_correo", sesion.usuario.correo.takeIf { solicitud.recordarCorreo }).apply()
                abrirSesion(sesion)
            }
        }
    }

    suspend fun iniciarGoogle(
        elegirDemo: (DesafioGoogle) -> Unit,
        obtenerToken: suspend (String, String) -> String
    ) {
        procesar {
            val registro = datos.value.registrando
            val desafio = repository.desafioGoogle()
            if (desafio.modo == "demo") {
                elegirDemo(desafio)
            } else {
                val clienteId = desafio.clienteId ?: throw ErrorCuentaGoogle("La API no entregó el cliente de Google.")
                val token = obtenerToken(clienteId, desafio.nonce)
                abrirSesion(repository.autenticarGoogle(CredencialGoogle(desafio.nonce, token = token), registro))
            }
        }
    }

    fun ingresarGoogleDemo(desafio: DesafioGoogle, cuenta: String) {
        viewModelScope.launch {
            procesar {
                val credencial = CredencialGoogle(desafio.nonce, cuentaDemo = cuenta)
                abrirSesion(repository.autenticarGoogle(credencial, datos.value.registrando))
            }
        }
    }

    fun avisar(mensaje: String) = datos.update { it.copy(errores = LoginErrores(general = mensaje)) }

    private fun abrirSesion(sesion: Sesion) {
        datos.update { it.copy(password = "", sesion = sesion) }
    }

    private suspend fun procesar(accion: suspend () -> Unit) {
        if (datos.value.isLoading) return
        datos.update { it.copy(isLoading = true, errores = LoginErrores()) }
        try {
            accion()
        } catch (error: CancellationException) {
            throw error
        } catch (error: ErrorCuentaGoogle) {
            avisar(error.message.orEmpty())
        } catch (error: Exception) {
            avisar(mensajeDe(error))
        } finally {
            datos.update { it.copy(isLoading = false) }
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
            } finally {
                GoogleCuentaAndroid.cerrar(getApplication<Application>())
            }
        }
    }
}
