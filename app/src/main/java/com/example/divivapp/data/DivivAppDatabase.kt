package com.example.divivapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Mesa::class,
        Comensal::class,
        Pedido::class,
        MenuItem::class
    ],
    version = 4,
    exportSchema = false // En produccion conviene true para mantener historial del esquema
)
abstract class DivivAppDatabase : RoomDatabase() {
    abstract fun mesaDao(): MesaDao
}
