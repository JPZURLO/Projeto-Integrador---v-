package com.example.gerokernel.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gerokernel.database.dao.ConsultaDao
import com.example.gerokernel.database.dao.MedicamentoDao
import com.example.gerokernel.model.entity.ConsultaEntity
import com.example.gerokernel.model.entity.MedicamentoEntity

// 🔥 ATENÇÃO AQUI: version = 6 para a base de dados se recriar sozinha!
@Database(entities = [MedicamentoEntity::class, ConsultaEntity::class], version = 6)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicamentoDao(): MedicamentoDao
    abstract fun consultaDao(): ConsultaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gerokernel_db"
                )
                    .fallbackToDestructiveMigration() // O nosso escudo anti-crash!
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}