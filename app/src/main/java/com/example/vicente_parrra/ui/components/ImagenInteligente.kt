package com.example.vicente_parrra.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun ImagenInteligente(imagen: Bitmap?) {
    val forma = Modifier.size(130.dp).clip(CircleShape)
    if (imagen == null) {
        Icon(Icons.Default.Person, contentDescription = "Perfil sin foto", modifier = forma)
    } else {
        Image(imagen.asImageBitmap(), contentDescription = "Foto de perfil", modifier = forma,
            contentScale = ContentScale.Crop)
    }
}
