package com.example.vicente_parrra

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

class AccesoScreenTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun ingresarVacioConservaElBotonActivoYMuestraValidaciones() {
        compose.onNodeWithText("Ingresar").performScrollTo().assertIsEnabled().performClick()
        compose.onNodeWithText("Ingresa tu correo.").assertExists()
        compose.onNodeWithText("Ingresa tu contraseña.").assertExists()
        compose.onNodeWithText("Ingresar").assertIsEnabled()
    }

    @Test
    fun crearCuentaMuestraLaOpcionDeGoogle() {
        compose.onNodeWithText("Ingresar con Google").assertExists()
        compose.onNodeWithText("Crear cuenta").performScrollTo().performClick()
        compose.onNodeWithText("Registrarse con Google").performScrollTo().assertIsEnabled()
        compose.onNodeWithText("Volver al ingreso").performScrollTo().performClick()
        compose.onNodeWithText("Ingresar con Google").assertExists()
    }
}
