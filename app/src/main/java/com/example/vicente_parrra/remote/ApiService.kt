package com.example.vicente_parrra.remote

import com.example.vicente_parrra.model.Post
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("posts")
    suspend fun listar(): List<Post>

    @POST("posts")
    suspend fun crear(@Body post: Post): Post

    @PUT("posts/{id}")
    suspend fun editar(@Path("id") id: Int, @Body post: Post): Post

    @DELETE("posts/{id}")
    suspend fun borrar(@Path("id") id: Int): Response<Unit>
}
