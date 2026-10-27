package com.example.vicente_parrra.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryTest {

    private val repository = AuthRepository()

    @Test
    fun usuariosObligatoriosInicianSesionConSuRol() = runBlocking {
        val usuarios = mapOf(
            "admin@guardian.test" to "Admin",
            "supervisor@guardian.test" to "Supervisor",
            "operador@guardian.test" to "Operador"
        )

        usuarios.forEach { (email, rolEsperado) ->
            val resultado = repository.autenticar(email, "123456")

            assertTrue(resultado.isSuccess)
            assertEquals(rolEsperado, resultado.getOrThrow().rol)
        }
    }

    @Test
    fun contraseñaIncorrectaDevuelveError() = runBlocking {
        val resultado = repository.autenticar("admin@guardian.test", "incorrecta")

        assertTrue(resultado.isFailure)
        assertEquals("Contraseña incorrecta", resultado.exceptionOrNull()?.message)
    }
}
