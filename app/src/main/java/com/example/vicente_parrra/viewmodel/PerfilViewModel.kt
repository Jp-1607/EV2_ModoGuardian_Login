package com.example.vicente_parrra.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PerfilViewModel : ViewModel() {
    private val foto = MutableStateFlow<Bitmap?>(null)
    val imagen = foto.asStateFlow()

    fun desdeGaleria(imagen: Bitmap) { foto.value = imagen }
    fun desdeCamara(imagen: Bitmap) { foto.value = imagen }
}
