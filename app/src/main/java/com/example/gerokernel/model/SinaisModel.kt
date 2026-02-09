package com.example.gerokernel.models

import com.google.gson.annotations.SerializedName

data class SinaisModel(
    val id: Int? = null,

    // O PULO DO GATO 🐈: Mapeia o snake_case (Banco) para camelCase (Android)
    @SerializedName("usuario_id")
    val usuarioId: Int,

    @SerializedName("data_hora")
    val data_hora: String? = null,

    val sistolica: Int,
    val diastolica: Int,
    val glicose: Int?
)