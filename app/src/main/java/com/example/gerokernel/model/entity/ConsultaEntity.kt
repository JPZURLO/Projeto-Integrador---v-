package com.example.gerokernel.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "consultas")
data class ConsultaEntity(
    @PrimaryKey(autoGenerate = true) val idLocal: Int = 0,
    val idRemoto: Int? = null,
    val medico: String,
    val especialidade: String,
    val dataHora: String,
    val localConsulta: String,
    val usuarioId: Int,
    val sincronizado: Boolean = true,
    val realizada: Boolean = false // 🔥 NOVO: Salva no SQLite se compareceu
)