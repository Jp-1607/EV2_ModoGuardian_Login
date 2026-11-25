package com.example.vicente_parrra

import com.example.vicente_parrra.model.EstadoPedido
import com.example.vicente_parrra.model.Rol
import com.example.vicente_parrra.model.Usuario
import com.example.vicente_parrra.navigation.Screen
import com.example.vicente_parrra.ui.screen.fechaValida
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlujoPedidoTest {
    @Test
    fun unaEntregaAvanzaEnOrden() {
        assertEquals(EstadoPedido.EN_RUTA, EstadoPedido.PENDIENTE.siguiente())
        assertEquals(EstadoPedido.ENTREGADO, EstadoPedido.EN_RUTA.siguiente())
        assertNull(EstadoPedido.ENTREGADO.siguiente())
    }

    @Test
    fun calendarioValidaFechasReales() {
        assertTrue(fechaValida("2028-02-29"))
        assertFalse(fechaValida("2026-02-29"))
        assertFalse(fechaValida("2026-11-4"))
    }

    @Test
    fun repartidorEntraAlDespachoSinAccesoComercial() {
        val repartidor = Usuario("test", "Prueba", "test@insertcode.test", Rol.REPARTIDOR, true,
            listOf("despacho", "actualizarEntrega"))
        assertTrue(Screen.Entregas.disponible(repartidor))
        assertFalse(Screen.NuevoPedido.disponible(repartidor))
        assertFalse(Screen.Usuarios.disponible(repartidor))
    }
}
