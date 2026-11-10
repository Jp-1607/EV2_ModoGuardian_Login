package com.example.vicente_parrra.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vicente_parrra.model.Post
import com.example.vicente_parrra.repository.PostRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PostEstado(
    val posts: List<Post> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)

class PostViewModel(private val repositorio: PostRepository = PostRepository()) : ViewModel() {
    private val datos = MutableStateFlow(PostEstado())
    val estado = datos.asStateFlow()

    init { cargar() }

    fun cargar() = ejecutar { repositorio.listar() }
    fun crear(titulo: String, texto: String) = ejecutar {
        val nuevo = repositorio.crear(titulo, texto)
        listOf(nuevo) + it.filterNot { post -> post.id == nuevo.id }
    }
    fun editar(post: Post) = ejecutar { lista ->
        val editado = repositorio.editar(post)
        lista.map { if (it.id == post.id) editado else it }
    }
    fun borrar(id: Int) = ejecutar {
        repositorio.borrar(id)
        it.filterNot { post -> post.id == id }
    }

    private fun ejecutar(accion: suspend (List<Post>) -> List<Post>) {
        val anterior = datos.value
        if (anterior.cargando) return
        datos.value = anterior.copy(cargando = true, error = null)
        viewModelScope.launch {
            try {
                datos.value = PostEstado(posts = accion(anterior.posts))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                datos.value = anterior.copy(error = "No se pudo completar la operación")
            }
        }
    }
}
