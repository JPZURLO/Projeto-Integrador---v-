package com.example.gerokernel.model

data class User(
    val id: Int,
    val nome: String,
    val email: String,
    val senha: String,
    val cpf: String,
    val data_nascimento: String,
    val tipo_usuario: String = "idoso"
)