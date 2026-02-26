package com.example.gerokernel.models
import com.example.gerokernel.model.entity.MedicamentoEntity
import java.util.Date

data class Medicamento(
    val id: Int,
    val nome_remedio: String,
    val dosagem: String,
    val quantidade_total: Int? = 0,
    val horario_agendado: Date? = null,
    var tomado: Boolean = false
)

fun Medicamento.toEntity(usuarioId: Int): MedicamentoEntity {
    return MedicamentoEntity(
        idRemoto = this.id,
        nome = this.nome_remedio,
        dosagem = this.dosagem,
        horario = this.horario_agendado.toString(), // Ajuste conforme seu formato de data
        usuarioId = usuarioId,
        sincronizado = true
    )
}