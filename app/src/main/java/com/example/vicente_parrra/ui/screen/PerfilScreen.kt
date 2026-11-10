package com.example.vicente_parrra.ui.screen

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.vicente_parrra.ui.components.ImagenInteligente
import com.example.vicente_parrra.viewmodel.PerfilViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PerfilScreen(modelo: PerfilViewModel) {
    val imagen by modelo.imagen.collectAsState()
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    var mensaje by remember { mutableStateOf("") }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch {
            val foto = withContext(Dispatchers.IO) {
                runCatching {
                    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    contexto.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
                    var escala = 1
                    while (maxOf(limites.outWidth, limites.outHeight) / escala > 1024) escala *= 2
                    contexto.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = escala })
                    }
                }.getOrNull()
            }
            if (foto != null) modelo.desdeGaleria(foto)
            mensaje = if (foto == null) "No se pudo abrir la imagen" else ""
        }
    }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { foto ->
        if (foto != null) modelo.desdeCamara(foto)
    }
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
        Text("Foto de perfil")
        ImagenInteligente(imagen)
        Button(onClick = { galeria.launch("image/*") }) { Text("Abrir galería") }
        Button(onClick = {
            mensaje = ""
            try { camara.launch(null) } catch (_: android.content.ActivityNotFoundException) {
                mensaje = "No hay una cámara disponible"
            }
        }) { Text("Tomar foto") }
        if (mensaje.isNotEmpty()) Text(mensaje)
    }
}
