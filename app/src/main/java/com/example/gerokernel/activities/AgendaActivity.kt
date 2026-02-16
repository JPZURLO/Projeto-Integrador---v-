package com.example.gerokernel.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gerokernel.R
import com.example.gerokernel.api.RetrofitClient
import com.example.gerokernel.models.*
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

class AgendaActivity : AppCompatActivity() {

    private var modoAtual = "CONSULTA"
    private var usuarioId = 0

    // Componentes de UI
    private lateinit var grupoConsulta: LinearLayout
    private lateinit var grupoRemedio: LinearLayout
    private lateinit var btnAbaConsulta: MaterialButton
    private lateinit var btnAbaRemedio: MaterialButton
    private lateinit var btnConfirmar: MaterialButton
    private lateinit var checkRemedios: CheckBox
    private lateinit var checkMedicos: CheckBox
    private lateinit var lblLista: TextView

    // Campos de entrada
    private lateinit var editMedico: EditText
    private lateinit var editEspecialidade: EditText
    private lateinit var editDataConsulta: EditText
    private lateinit var editHoraConsulta: EditText
    private lateinit var editNomeRemedio: EditText
    private lateinit var editDosagem: EditText
    private lateinit var editEstoque: EditText
    private lateinit var editHoraInicio: EditText
    private lateinit var radioFrequencia: RadioGroup

    // RecyclerView e Dados
    private lateinit var recyclerAgenda: RecyclerView
    private lateinit var adapter: AgendaMedicamentoAdapter
    private var listaMedicamentos: List<Medicamento> = emptyList()
    private var listaConsultas: List<ConsultaModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda)

        val prefs = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)
        usuarioId = prefs.getInt("ID_USUARIO", 0)

        inicializarComponentes()
        configurarCliquesAbas()
        configurarRelogios()
        configurarFiltros()
        carregarDadosGeral() // Inicia o pipeline de busca
    }

    private fun inicializarComponentes() {
        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener { finish() }

        btnAbaConsulta = findViewById(R.id.btnAbaConsulta)
        btnAbaRemedio = findViewById(R.id.btnAbaRemedio)
        grupoConsulta = findViewById(R.id.grupoConsulta)
        grupoRemedio = findViewById(R.id.grupoRemedio)
        btnConfirmar = findViewById(R.id.btnConfirmarGeral)
        checkRemedios = findViewById(R.id.checkRemedios)
        checkMedicos = findViewById(R.id.checkMedicos)
        lblLista = findViewById(R.id.lblLista)

        editMedico = findViewById(R.id.editMedico)
        editEspecialidade = findViewById(R.id.editEspecialidade)
        editDataConsulta = findViewById(R.id.editDataConsulta)
        editHoraConsulta = findViewById(R.id.editHoraConsulta)

        editNomeRemedio = findViewById(R.id.editNomeRemedio)
        editDosagem = findViewById(R.id.editDosagem)
        editEstoque = findViewById(R.id.editEstoque)
        editHoraInicio = findViewById(R.id.editHoraInicio)
        radioFrequencia = findViewById(R.id.radioFrequencia)

        recyclerAgenda = findViewById(R.id.recyclerAgenda)
        recyclerAgenda.layoutManager = LinearLayoutManager(this)

        // Inicializa o adapter com os callbacks de clique
        adapter = AgendaMedicamentoAdapter(this,
            { id -> tomarRemedio(id) },
            { id -> excluirDose(id) }
        )
        recyclerAgenda.adapter = adapter

        btnConfirmar.setOnClickListener {
            if (modoAtual == "CONSULTA") salvarConsulta() else salvarRemedio()
        }
    }

    private fun configurarRelogios() {
        val cal = Calendar.getInstance()

        // Seletores para Consultas
        editDataConsulta.setOnClickListener {
            DatePickerDialog(this, { _, ano, mes, dia ->
                val data = String.format(Locale.getDefault(), "%02d/%02d/%d", dia, mes + 1, ano)
                editDataConsulta.setText(data)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        editHoraConsulta.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                val hora = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                editHoraConsulta.setText(hora)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        // Seletor para Remédios
        editHoraInicio.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                val hora = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                editHoraInicio.setText(hora)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }
    }

    private fun configurarFiltros() {
        val listenerFiltro = { _: View -> aplicarFiltros() }
        checkRemedios.setOnClickListener(listenerFiltro)
        checkMedicos.setOnClickListener(listenerFiltro)
    }

    private fun carregarDadosGeral() {
        // Busca Medicamentos primeiro
        RetrofitClient.instance.getMedicamentos(usuarioId).enqueue(object : Callback<List<Medicamento>> {
            override fun onResponse(call: Call<List<Medicamento>>, response: Response<List<Medicamento>>) {
                if (response.isSuccessful) {
                    listaMedicamentos = response.body() ?: emptyList()
                    buscarConsultas() // Depois busca as consultas
                }
            }
            override fun onFailure(call: Call<List<Medicamento>>, t: Throwable) {
                Log.e("AGENDA_DEBUG", "Erro medicamentos: ${t.message}")
            }
        })
    }

    private fun buscarConsultas() {
        RetrofitClient.instance.listarConsultas(usuarioId).enqueue(object : Callback<List<ConsultaModel>> {
            override fun onResponse(call: Call<List<ConsultaModel>>, response: Response<List<ConsultaModel>>) {
                if (response.isSuccessful) {
                    listaConsultas = response.body() ?: emptyList()
                    aplicarFiltros() // Atualiza a lista unificada
                }
            }
            override fun onFailure(call: Call<List<ConsultaModel>>, t: Throwable) {
                Log.e("AGENDA_DEBUG", "Erro consultas: ${t.message}")
            }
        })
    }

    private fun aplicarFiltros() {
        val exibirRemedios = checkRemedios.isChecked
        val exibirMedicos = checkMedicos.isChecked
        val listaGeral = mutableListOf<Any>()

        if (exibirRemedios) listaGeral.addAll(listaMedicamentos)
        if (exibirMedicos) listaGeral.addAll(listaConsultas)

        adapter.atualizarLista(listaGeral)
        lblLista.text = if (listaGeral.isEmpty()) "Nenhum item selecionado" else "Agenda de Hoje"
    }

    private fun salvarConsulta() {
        val medico = editMedico.text.toString()
        val especialidade = editEspecialidade.text.toString()
        val data = editDataConsulta.text.toString()
        val hora = editHoraConsulta.text.toString()

        if (medico.isEmpty() || data.isEmpty() || hora.isEmpty()) {
            Toast.makeText(this, "Preencha Médico, Data e Hora!", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ConsultaModel(
            usuarioId = usuarioId,
            medico = medico,
            especialidade = especialidade,
            dataHora = "$data $hora",
            local = "Não informado"
        )

        RetrofitClient.instance.salvarConsulta(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AgendaActivity, "Consulta Agendada! 🩺", Toast.LENGTH_SHORT).show()
                    limparCampos()
                    carregarDadosGeral() // Recarrega para mostrar na lista
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("AGENDA_DEBUG", "Erro salvar consulta: ${t.message}")
            }
        })
    }

    private fun salvarRemedio() {
        val nome = editNomeRemedio.text.toString()
        val hora = editHoraInicio.text.toString()

        if (nome.isEmpty() || hora.isEmpty()) {
            Toast.makeText(this, "Preencha o nome e a hora!", Toast.LENGTH_SHORT).show()
            return
        }

        val frequencia = when (radioFrequencia.checkedRadioButtonId) {
            R.id.rb8h -> 8
            R.id.rb12h -> 12
            R.id.rb24h -> 24
            else -> 0
        }

        val request = MedicamentoRequest(
            usuario_id = usuarioId,
            nome_remedio = nome,
            dosagem = editDosagem.text.toString(),
            horario_inicio = hora,
            frequencia_horas = frequencia,
            quantidade_total = editEstoque.text.toString().toIntOrNull() ?: 0
        )

        RetrofitClient.instance.salvarMedicamento(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@AgendaActivity, "Cronograma Gerado! ✅", Toast.LENGTH_SHORT).show()
                    limparCampos()
                    carregarDadosGeral()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("AGENDA_DEBUG", "Erro salvar remédio: ${t.message}")
            }
        })
    }

    private fun tomarRemedio(id: Int) {
        RetrofitClient.instance.tomarMedicamento(id).enqueue(object : Callback<TomarMedicamentoResponse> {
            override fun onResponse(call: Call<TomarMedicamentoResponse>, response: Response<TomarMedicamentoResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(applicationContext, "Tomado!", Toast.LENGTH_SHORT).show()
                    carregarDadosGeral()
                }
            }
            override fun onFailure(call: Call<TomarMedicamentoResponse>, t: Throwable) {
                Log.e("AGENDA_DEBUG", "Erro tomar: ${t.message}")
            }
        })
    }

    private fun excluirDose(id: Int) {
        // 1. Precisamos identificar o item na nossa lista atual para saber se é Medicamento ou Consulta
        val itemParaExcluir = listaMedicamentos.find { it.id == id }

        if (itemParaExcluir != null) {
            // É um MEDICAMENTO: chama a rota de remédios
            RetrofitClient.instance.excluirMedicamento(id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AgendaActivity, "Remédio removido! 💊", Toast.LENGTH_SHORT).show()
                        carregarDadosGeral() // Recarrega a lista híbrida
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("AGENDA_DEBUG", "Erro ao excluir remédio: ${t.message}")
                }
            })
        } else {
            // Não achou nos medicamentos? Então é uma CONSULTA: chama a rota de médicos
            RetrofitClient.instance.deletarConsulta(id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@AgendaActivity, "Consulta desmarcada! 🩺", Toast.LENGTH_SHORT).show()
                        carregarDadosGeral()
                    } else {
                        // Se der erro P2025 no servidor, o log vai aparecer aqui
                        Log.e("AGENDA_DEBUG", "Erro servidor: ${response.code()}")
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("AGENDA_DEBUG", "Erro ao excluir consulta: ${t.message}")
                }
            })
        }
    }

    private fun configurarCliquesAbas() {
        btnAbaConsulta.setOnClickListener {
            modoAtual = "CONSULTA"
            grupoConsulta.visibility = View.VISIBLE
            grupoRemedio.visibility = View.GONE
            btnAbaConsulta.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            btnAbaRemedio.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E0E0E0"))
            btnConfirmar.text = "AGENDAR CONSULTA"
            btnConfirmar.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2E7D32"))
        }

        btnAbaRemedio.setOnClickListener {
            modoAtual = "REMEDIO"
            grupoConsulta.visibility = View.GONE
            grupoRemedio.visibility = View.VISIBLE
            btnAbaRemedio.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            btnAbaConsulta.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E0E0E0"))
            btnConfirmar.text = "SALVAR REMÉDIO"
            btnConfirmar.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1976D2"))
        }
    }

    private fun limparCampos() {
        editNomeRemedio.text.clear()
        editDosagem.text.clear()
        editEstoque.text.clear()
        editHoraInicio.text.clear()
        editMedico.text.clear()
        editEspecialidade.text.clear()
        editDataConsulta.text.clear()
        editHoraConsulta.text.clear()
        radioFrequencia.clearCheck()
    }
}