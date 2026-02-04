package com.example.gerokernel.models

// O que o Back devolve (GET) e o que a gente manda (POST)
data class SinaisModel(
    val id: Int? = null,
    val usuarioId: Int,
    val sistolica: Int,
    val diastolica: Int,
    val glicose: Int?,
    val data_hora: String? = null // O banco manda data, mas no POST não precisamos mandar
)