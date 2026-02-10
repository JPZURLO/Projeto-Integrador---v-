package com.example.gerokernel.models

data class HidratacaoRequest(
    val usuario_id: Int,
    val quantidade_ml: Int
)

data class HidratacaoResponse(
    val id: Int,
    val quantidade_ml: Int,
    val data_hora: String
)