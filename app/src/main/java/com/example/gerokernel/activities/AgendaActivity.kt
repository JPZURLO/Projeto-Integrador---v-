package com.example.gerokernel.activities

import android.app.*
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gerokernel.adapters.ConsultasAdapter
import com.example.gerokernel.databinding.ActivityAgendaBinding
import com.example.gerokernel.models.ConsultaModel
import com.example.gerokernel.utils.LembreteReceiver
import java.text.SimpleDateFormat
import java.util.*

class AgendaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgendaBinding

    private val consultas = mutableListOf<ConsultaModel>()
    private var consultaEmEdicao: ConsultaModel? = null

    private var dataSelecionada: String? = null
    private var horaSelecionada: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgendaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        setupClicks()
    }

    private fun setupRecycler() {
        binding.recyclerConsultas.layoutManager = LinearLayoutManager(this)
        binding.recyclerConsultas.adapter =
            ConsultasAdapter(
                lista = consultas,
                onEditarClick = { editarConsulta(it) },
                onDeleteClick = { excluirConsultaPorId(it) }
            )
    }

    private fun setupClicks() {
        binding.btnData.setOnClickListener { abrirDatePicker() }
        binding.btnHora.setOnClickListener { abrirTimePicker() }
        binding.btnAgendar.setOnClickListener { salvarOuEditarConsulta() }
        binding.btnVoltar.setOnClickListener { finish() }
    }

    // ---------------- DATA / HORA ----------------

    private fun abrirDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, y, m, d ->
                dataSelecionada = "%04d-%02d-%02d".format(y, m + 1, d)
                atualizarTextoDataHora()
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun abrirTimePicker() {
        val c = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, h, m ->
                horaSelecionada = "%02d:%02d".format(h, m)
                atualizarTextoDataHora()
            },
            c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun atualizarTextoDataHora() {
        if (dataSelecionada != null && horaSelecionada != null) {
            binding.txtDataSelecionada.text =
                "$dataSelecionada $horaSelecionada"
        }
    }

    // ---------------- SALVAR / EDITAR ----------------

    private fun salvarOuEditarConsulta() {

        val medico = binding.editMedico.text.toString()
        val especialidade = binding.editEspecialidade.text.toString()

        if (
            medico.isBlank() ||
            especialidade.isBlank() ||
            dataSelecionada == null ||
            horaSelecionada == null
        ) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val dataHora = "$dataSelecionada $horaSelecionada"

        if (consultaEmEdicao == null) {

            val nova = ConsultaModel(
                id = consultas.size + 1,
                usuarioId = 1,
                medico = medico,
                especialidade = especialidade,
                dataHora = dataHora,
                local = "Consultório"
            )

            consultas.add(nova)
            agendarLembrete(nova)

            Toast.makeText(this, "Consulta agendada!", Toast.LENGTH_SHORT).show()

        } else {

            val index = consultas.indexOfFirst { it.id == consultaEmEdicao!!.id }

            val atualizada =
                consultaEmEdicao!!.copy(
                    medico = medico,
                    especialidade = especialidade,
                    dataHora = dataHora
                )

            consultas[index] = atualizada
            agendarLembrete(atualizada)

            consultaEmEdicao = null
            binding.btnAgendar.text = "CONFIRMAR AGENDAMENTO"

            Toast.makeText(this, "Consulta atualizada!", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerConsultas.adapter?.notifyDataSetChanged()
        limparFormulario()
    }

    // ---------------- 🔔 AGENDAR LEMBRETE ----------------

    private fun agendarLembrete(consulta: ConsultaModel) {

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, LembreteReceiver::class.java).apply {
            putExtra("MEDICO", consulta.medico)
            putExtra("ESPECIALIDADE", consulta.especialidade)
            putExtra("ID_CONSULTA", consulta.id ?: 0)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            consulta.id ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dataConsulta = sdf.parse(consulta.dataHora) ?: return

        // ⏰ 1 minuto antes
        val tempoLembrete = dataConsulta.time - (2 * 60 * 60 * 1000)

        if (tempoLembrete <= System.currentTimeMillis()) return

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            tempoLembrete,
            pendingIntent
        )
    }

    // ---------------- EDITAR / EXCLUIR ----------------

    private fun editarConsulta(c: ConsultaModel) {
        consultaEmEdicao = c

        binding.editMedico.setText(c.medico)
        binding.editEspecialidade.setText(c.especialidade)

        val partes = c.dataHora.split(" ")
        dataSelecionada = partes[0]
        horaSelecionada = partes[1]

        atualizarTextoDataHora()
        binding.btnAgendar.text = "SALVAR ALTERAÇÃO"
    }

    private fun excluirConsultaPorId(id: Int) {
        consultas.removeIf { it.id == id }
        binding.recyclerConsultas.adapter?.notifyDataSetChanged()
        Toast.makeText(this, "Consulta excluída", Toast.LENGTH_SHORT).show()
    }

    private fun limparFormulario() {
        binding.editMedico.text?.clear()
        binding.editEspecialidade.text?.clear()
        binding.txtDataSelecionada.text = "Selecione data e hora..."
        dataSelecionada = null
        horaSelecionada = null
    }
}