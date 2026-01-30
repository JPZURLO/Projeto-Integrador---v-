package com.example.gerokernel.model

data class SinalVital(
    val pressaoSistolica: Int,
    val pressaoDiastolica: Int,
    val glicose: Double,
    val dataHora: Long = System.currentTimeMillis()
)