package com.example.vicente_parrra

import com.example.vicente_parrra.model.Post
import com.example.vicente_parrra.remote.ApiService
import com.example.vicente_parrra.repository.PostRepository
import com.example.vicente_parrra.viewmodel.PostViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class PostTest : StringSpec({
    val post = Post(1, "Aviso", "Revisar entrega")

    "El repositorio devuelve los posts de la API" {
        val api = mockk<ApiService>()
        coEvery { api.listar() } returns listOf(post)
        PostRepository(api).listar() shouldBe listOf(post)
    }

    "El ViewModel muestra los posts al terminar la carga" {
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        try {
            val repositorio = mockk<PostRepository>()
            coEvery { repositorio.listar() } returns listOf(post)
            val modelo = PostViewModel(repositorio)
            modelo.estado.value.cargando shouldBe true
            dispatcher.scheduler.runCurrent()
            modelo.estado.value.cargando shouldBe false
            modelo.estado.value.posts shouldBe listOf(post)
        } finally {
            Dispatchers.resetMain()
        }
    }
})
