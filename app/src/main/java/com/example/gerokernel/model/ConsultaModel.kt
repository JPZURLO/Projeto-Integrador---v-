package com.example.gerokernel.models

import com.google.gson.annotations.SerializedName

data class ConsultaModel(
    @SerializedName("usuario_id")
    val usuarioId: Int,

    val medico: String,
    val especialidade: String,

    @SerializedName("data_hora")
    val dataHora: String,

    val local: String? = "Clínica Geral",

    val id: Int? = null,
    var realizada: Boolean = false // 🔥 NOVO: Marca se já foi ao médico
)