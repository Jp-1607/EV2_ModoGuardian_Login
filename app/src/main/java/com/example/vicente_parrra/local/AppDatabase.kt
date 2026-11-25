package com.example.vicente_parrra.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert

@Entity(tableName = "operacion_local")
data class OperacionLocal(@PrimaryKey val cuenta: String, val contenido: String, val actualizada: Long)

@Dao
interface DistribucionDao {
    @Query("SELECT * FROM operacion_local WHERE cuenta = :cuenta")
    suspend fun consultar(cuenta: String): OperacionLocal?

    @Upsert
    suspend fun guardar(operacion: OperacionLocal)

    @Query("DELETE FROM operacion_local WHERE cuenta = :cuenta")
    suspend fun borrar(cuenta: String)
}

@Database(entities = [OperacionLocal::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun distribucion(): DistribucionDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun obtener(contexto: Context): AppDatabase = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                contexto.applicationContext, AppDatabase::class.java, "insertcode.db"
            ).build().also { instancia = it }
        }
    }
}
