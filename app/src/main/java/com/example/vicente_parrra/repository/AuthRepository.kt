package com.example.vicente_parrra.repository

import com.example.vicente_parrra.model.UsuarioGuardian
import kotlinx.coroutines.delay

class AuthRepository {

    private val usuarios = listOf(
        UsuarioGuardian("admin@guardian.test", "Admin", "Administrador"),
        UsuarioGuardian("supervisor@guardian.test", "Supervisor", "Supervisor"),
        UsuarioGuardian("operador@guardian.test", "Operador", "Operador")
    )

    suspend fun autenticar(email: String, password: String): Result<UsuarioGuardian> {
        delay(800)

        if (password != "123456") {
            return Result.failure(Exception("Contraseña incorrecta"))
        }

        val usuario = usuarios.find {
            it.email.equals(email.trim(), ignoreCase = true)
        }

        return if (usuario != null) {
            Result.success(usuario)
        } else {
            Result.failure(Exception("Usuario no encontrado"))
        }
    }
}
