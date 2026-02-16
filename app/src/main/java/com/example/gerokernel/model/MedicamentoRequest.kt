package com.example.gerokernel.models

data class MedicamentoRequest(
    val usuario_id: Int,
    val nome_remedio: String,
    val dosagem: String,
    val horario_inicio: String,
    val frequencia_horas: Int,
    val quantidade_total: Int
)