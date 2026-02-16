package com.example.gerokernel.models

// O que vem do servidor
data class FichaMedicaResponse(
    val tipo_sanguineo: String?,
    val alergias: String?,
    val doencas_cronicas: String?,
    val contato_nome: String?,
    val contato_telefone: String?,
    val lista_medicamentos: List<Medicamento>?
)
