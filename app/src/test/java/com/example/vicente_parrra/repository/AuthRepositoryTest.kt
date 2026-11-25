package com.example.vicente_parrra.repository

import com.example.vicente_parrra.model.Credenciales
import com.example.vicente_parrra.model.Rol
import com.example.vicente_parrra.model.Sesion
import com.example.vicente_parrra.model.Usuario
import com.example.vicente_parrra.remote.ApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class AuthRepositoryTest {
    private val api = mockk<ApiService>()
    private val repositorio = AuthRepository(api)
    private val usuario = Usuario("v1", "Ventas de prueba", "ventas@insertcode.test", Rol.VENDEDOR, true, listOf("pedidos"))
    private val sesion = Sesion(usuario, "token-de-prueba")

    @Test
    fun enviaElCorreoSinEspaciosYRecuperaElRolDeLaApi() = runTest {
        coEvery { api.autenticar(Credenciales(usuario.correo, "clave-de-test")) } returns sesion
        assertEquals(Rol.VENDEDOR, repositorio.autenticar("  ${usuario.correo} ", "clave-de-test").usuario.rol)
    }

    @Test
    fun cierraLaSesionDelServidor() = runTest {
        coEvery { api.cerrar("Bearer ${sesion.token}") } returns Response.success(Unit)
        repositorio.cerrar(sesion)
        coVerify(exactly = 1) { api.cerrar("Bearer ${sesion.token}") }
    }
}
