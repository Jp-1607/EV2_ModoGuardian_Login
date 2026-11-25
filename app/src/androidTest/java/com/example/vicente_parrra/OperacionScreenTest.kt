package com.example.vicente_parrra

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.vicente_parrra.model.Entrega
import com.example.vicente_parrra.model.EstadoPedido
import com.example.vicente_parrra.model.Rol
import com.example.vicente_parrra.model.Usuario
import com.example.vicente_parrra.ui.screen.EntregasScreen
import com.example.vicente_parrra.ui.screen.HomeScreen
import com.example.vicente_parrra.ui.theme.Vicente_parrraTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OperacionScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun vendedorVeNuevoPedidoPeroNoAdministracion() {
        val vendedor = Usuario("test", "Ventas de prueba", "test@insertcode.test", Rol.VENDEDOR, true,
            listOf("comercial", "pedidos", "crearPedido"))
        compose.setContent { Vicente_parrraTheme { HomeScreen(vendedor, null) {} } }
        compose.onNodeWithText("Nuevo pedido").assertIsDisplayed()
        compose.onNodeWithText("Usuarios").assertDoesNotExist()
    }

    @Test
    fun repartidorPuedeIniciarUnDespachoPendiente() {
        val entrega = Entrega("ENT-001", "PED-001", "R-01", 1, "Cliente de prueba",
            "Punto de prueba", "09:00", EstadoPedido.PENDIENTE, -33.45, -70.65)
        var seleccion: Entrega? = null
        compose.setContent {
            Vicente_parrraTheme { EntregasScreen(listOf(entrega), true) { seleccion = it } }
        }
        compose.onNodeWithText("Iniciar despacho").performClick()
        compose.runOnIdle { assertEquals(entrega, seleccion) }
    }
}
