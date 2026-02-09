package com.example.gerokernel.models

import com.google.gson.annotations.SerializedName

data class ConsultaModel(
    val id: Int? = null,

    @SerializedName("usuario_id")
    val usuarioId: Int,

    val medico: String,
    val especialidade: String,

    @SerializedName("data_hora")
    val dataHora: String,

    val local: String? = ""
)