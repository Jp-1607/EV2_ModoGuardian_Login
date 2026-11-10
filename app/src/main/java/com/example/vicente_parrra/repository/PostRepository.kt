package com.example.vicente_parrra.repository

import com.example.vicente_parrra.model.Post
import com.example.vicente_parrra.remote.ApiService
import com.example.vicente_parrra.remote.RetrofitInstance

class PostRepository(private val api: ApiService = RetrofitInstance.api) {
    suspend fun listar() = api.listar()
    suspend fun crear(titulo: String, texto: String) = api.crear(Post(0, titulo, texto))
    suspend fun editar(post: Post) = api.editar(post.id, post)
    suspend fun borrar(id: Int) {
        check(api.borrar(id).isSuccessful)
    }
}
