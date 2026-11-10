package com.example.vicente_parrra.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.vicente_parrra.model.UsuarioErrores
import com.example.vicente_parrra.model.UsuarioUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UsuarioViewModel : ViewModel() {
    private val datos = MutableStateFlow(UsuarioUiState())
    val estado = datos.asStateFlow()

    fun nombre(valor: String) = datos.update { it.copy(nombre = valor, errores = it.errores.copy(nombre = null)) }
    fun correo(valor: String) = datos.update { it.copy(correo = valor, errores = it.errores.copy(correo = null)) }
    fun clave(valor: String) = datos.update { it.copy(clave = valor, errores = it.errores.copy(clave = null)) }

    fun validarFormulario(): Boolean {
        val actual = datos.value
        val errores = UsuarioErrores(
            nombre = if (actual.nombre.trim().length < 2) "Ingresa tu nombre" else null,
            correo = if (!Patterns.EMAIL_ADDRESS.matcher(actual.correo.trim()).matches()) "Ingresa un correo válido" else null,
            clave = if (actual.clave.length < 6) "Usa al menos 6 caracteres" else null
        )
        val valido = errores == UsuarioErrores()
        datos.value = actual.copy(clave = if (valido) "" else actual.clave, errores = errores)
        return valido
    }
}
