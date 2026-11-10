package com.example.vicente_parrra.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val Context.preferenciasModo by preferencesDataStore("modo_guardian")

class ModoViewModel(app: Application) : AndroidViewModel(app) {
    private val datos = app.preferenciasModo
    private val clave = booleanPreferencesKey("modo_activo")
    val activo: StateFlow<Boolean?> = datos.data
        .map<androidx.datastore.preferences.core.Preferences, Boolean?> { it[clave] ?: false }
        .catch { if (it is java.io.IOException) emit(false) else throw it }
        .onStart { delay(500) }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val aviso = MutableStateFlow("")
    val mensaje = aviso.asStateFlow()
    private var guardando = false

    fun cambiarModo() {
        if (guardando) return
        guardando = true
        viewModelScope.launch {
            try {
                datos.edit { it[clave] = !(it[clave] ?: false) }
                aviso.value = "Estado guardado"
                delay(1300)
            } catch (_: java.io.IOException) {
                aviso.value = "No se pudo guardar"
                delay(1300)
            } finally {
                aviso.value = ""
                guardando = false
            }
        }
    }
}
