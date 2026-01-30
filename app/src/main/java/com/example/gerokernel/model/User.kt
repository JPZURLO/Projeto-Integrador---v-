package com.exemplo.gerokernel.models

data class User(
    val nome: String,
    val email: String,
    val senha: String,
    val cpf: String,
    val data_nascimento: String,
    val tipo_usuario: String = "idoso"
)