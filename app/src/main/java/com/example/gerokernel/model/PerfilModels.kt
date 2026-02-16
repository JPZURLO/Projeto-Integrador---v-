package com.example.gerokernel.models

data class AtualizarPerfilRequest(
    val nome: String,
    val email: String,
    val telefone: String,
    val tipo_sanguineo: String,
    val alergias: String
)