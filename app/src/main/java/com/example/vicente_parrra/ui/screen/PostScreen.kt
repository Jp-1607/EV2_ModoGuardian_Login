package com.example.vicente_parrra.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.vicente_parrra.model.Post
import com.example.vicente_parrra.viewmodel.PostEstado
import com.example.vicente_parrra.viewmodel.PostViewModel

@Composable
fun PostScreen(modelo: PostViewModel) {
    val estado by modelo.estado.collectAsState()
    ListaPosts(estado, modelo::cargar, modelo::crear, modelo::editar, modelo::borrar)
}

@Composable
fun ListaPosts(estado: PostEstado, cargar: () -> Unit, crear: (String, String) -> Unit,
    editar: (Post) -> Unit, borrar: (Int) -> Unit) {
    var titulo by remember { mutableStateOf("") }
    var texto by remember { mutableStateOf("") }
    var seleccionado by remember { mutableStateOf<Post?>(null) }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Publicaciones", style = MaterialTheme.typography.headlineSmall)
            Text("API de práctica: los cambios son simulados.")
            OutlinedTextField(titulo, { titulo = it }, label = { Text("Título") })
            OutlinedTextField(texto, { texto = it }, label = { Text("Contenido") })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = !estado.cargando && titulo.isNotBlank() && texto.isNotBlank(),
                    onClick = {
                        val post = seleccionado
                        if (post == null) crear(titulo, texto) else editar(post.copy(title = titulo, body = texto))
                        seleccionado = null
                        titulo = ""
                        texto = ""
                    }) { Text(if (seleccionado == null) "Publicar" else "Guardar") }
                TextButton(onClick = cargar, enabled = !estado.cargando) { Text("Recargar") }
            }
            if (estado.cargando) CircularProgressIndicator()
            estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        items(estado.posts, key = { it.id }) { post ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(post.title, style = MaterialTheme.typography.titleMedium)
                    Text(post.body)
                    if (post.id in 1..100) {
                        Row {
                            TextButton(enabled = !estado.cargando, onClick = {
                                seleccionado = post
                                titulo = post.title
                                texto = post.body
                            }) { Text("Editar") }
                            TextButton(enabled = !estado.cargando, onClick = {
                                if (seleccionado?.id == post.id) {
                                    seleccionado = null
                                    titulo = ""
                                    texto = ""
                                }
                                borrar(post.id)
                            }) { Text("Borrar") }
                        }
                    }
                }
            }
        }
    }
}
