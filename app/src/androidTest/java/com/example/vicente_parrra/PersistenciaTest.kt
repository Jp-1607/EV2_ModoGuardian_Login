package com.example.vicente_parrra

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.vicente_parrra.local.AppDatabase
import com.example.vicente_parrra.local.OperacionLocal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersistenciaTest {
    @Test
    fun roomMantieneLaCopiaSoloParaSuCuenta() = runBlocking {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val base = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        try {
            val dao = base.distribucion()
            val copia = OperacionLocal("vendedor:VENTAS", "{\"pedidos\":[]}", 1)
            dao.guardar(copia)
            assertEquals(copia, dao.consultar(copia.cuenta))
            assertNull(dao.consultar("repartidor:DESPACHO"))
            val nueva = copia.copy(actualizada = 2L)
            dao.guardar(nueva)
            assertEquals(nueva, dao.consultar(copia.cuenta))
            dao.borrar(copia.cuenta)
            assertNull(dao.consultar(copia.cuenta))
        } finally {
            base.close()
        }
    }
}
