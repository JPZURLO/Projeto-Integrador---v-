package com.example.gerokernel.models
import java.util.Date

data class Medicamento(
    val id: Int,
    val nome_remedio: String,
    val dosagem: String,
    val quantidade_total: Int?,
    val horario_agendado: Date?, // O Retrofit/Gson converterá o ISO do Node.js para Date automaticamente
    val estoque_minimo: Int? = 5
)