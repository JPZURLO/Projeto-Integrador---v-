package com.example.gerokernel.models

data class TomarMedicamentoResponse(
    val id: Int,
    val quantidade_total: Int,
    val alerta_estoque: Boolean? = false,
    val mensagem: String? = null
)