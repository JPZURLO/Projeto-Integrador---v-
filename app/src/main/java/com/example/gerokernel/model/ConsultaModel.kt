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

    // El ID es deixa al final com a opcional per evitar errors en crear l'objecte
    val id: Int? = null
)