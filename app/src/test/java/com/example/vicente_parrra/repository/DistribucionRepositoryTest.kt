package com.example.vicente_parrra.repository

import com.example.vicente_parrra.local.DistribucionDao
import com.example.vicente_parrra.local.OperacionLocal
import com.example.vicente_parrra.model.Operacion
import com.example.vicente_parrra.model.Rol
import com.example.vicente_parrra.model.Sesion
import com.example.vicente_parrra.model.Usuario
import com.example.vicente_parrra.remote.ApiService
import com.google.gson.Gson
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot

class DistribucionRepositoryTest : StringSpec({
    val usuario = Usuario("sup", "Supervisión", "sup@insertcode.test", Rol.SUPERVISOR, true, listOf("pedidos", "indicadores"))
    val sesion = Sesion(usuario, "token-de-prueba")
    val operacion = Operacion(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), null)
    val cuenta = "sup:SUPERVISOR:indicadores,pedidos"

    "Guarda la operación de la cuenta y el rol que la consultó" {
        val dao = mockk<DistribucionDao>()
        val api = mockk<ApiService>()
        val guardado = slot<OperacionLocal>()
        coEvery { api.consultar("Bearer ${sesion.token}") } returns operacion
        coEvery { dao.guardar(capture(guardado)) } returns Unit
        val resultado = DistribucionRepository(dao, api).actualizar(sesion)
        resultado.first shouldBe operacion
        guardado.captured.cuenta shouldBe cuenta
        guardado.captured.actualizada shouldBe resultado.second
    }

    "Lee la copia local sin consultar la API" {
        val dao = mockk<DistribucionDao>()
        val api = mockk<ApiService>()
        coEvery { dao.consultar(cuenta) } returns OperacionLocal(cuenta, Gson().toJson(operacion), 45)
        DistribucionRepository(dao, api).local(sesion) shouldBe (operacion to 45L)
        coVerify(exactly = 0) { api.consultar(any()) }
    }
})
