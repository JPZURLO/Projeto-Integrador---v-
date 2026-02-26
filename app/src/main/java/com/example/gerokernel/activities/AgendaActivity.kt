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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gerokernel.R
import com.example.gerokernel.api.RetrofitClient
import com.example.gerokernel.database.AppDatabase
import com.example.gerokernel.models.*
import com.example.gerokernel.model.entity.MedicamentoEntity
import com.example.gerokernel.model.entity.ConsultaEntity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgendaActivity : AppCompatActivity() {

    private var modoAtual = "CONSULTA"
    private var usuarioId = 0
    private lateinit var database: AppDatabase

    private lateinit var grupoConsulta: LinearLayout
    private lateinit var grupoRemedio: LinearLayout
    private lateinit var btnAbaConsulta: MaterialButton
    private lateinit var btnAbaRemedio: MaterialButton
    private lateinit var btnConfirmar: MaterialButton
    private lateinit var checkRemedios: CheckBox
    private lateinit var checkMedicos: CheckBox
    private lateinit var lblLista: TextView

    private lateinit var editMedico: EditText
    private lateinit var editEspecialidade: EditText
    private lateinit var editDataConsulta: EditText
    private lateinit var editHoraConsulta: EditText
    private lateinit var editNomeRemedio: EditText
    private lateinit var editDosagem: EditText
    private lateinit var editEstoque: EditText
    private lateinit var editHoraInicio: EditText
    private lateinit var radioFrequencia: RadioGroup

    private lateinit var recyclerAgenda: RecyclerView
    private lateinit var adapter: AgendaMedicamentoAdapter
    private var listaMedicamentos: List<Medicamento> = emptyList()
    private var listaConsultas: List<ConsultaModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda)

        val prefs = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)
        usuarioId = prefs.getInt("ID_USUARIO", 0)

        database = AppDatabase.getDatabase(this)

        inicializarComponentes()
        configurarCliquesAbas()
        configurarRelogios()
        configurarFiltros()

        carregarDadosGeral()
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

        adapter = AgendaMedicamentoAdapter(this,
            { med -> confirmarTomarRemedio(med) },
            { consulta -> confirmarPresencaConsulta(consulta) }, // 🔥 PASSA A CONSULTA AQUI!
            { id -> excluirDose(id) }
        )
        recyclerAgenda.adapter = adapter

        btnConfirmar.setOnClickListener {
            if (modoAtual == "CONSULTA") salvarConsulta() else salvarRemedio()
        }
    }

    private fun configurarRelogios() {
        val cal = Calendar.getInstance()
        editDataConsulta.setOnClickListener {
            DatePickerDialog(this, { _, ano, mes, dia ->
                editDataConsulta.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", dia, mes + 1, ano))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        editHoraConsulta.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                editHoraConsulta.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }
        editHoraInicio.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                editHoraInicio.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }
    }

    private fun configurarFiltros() {
        val listenerFiltro = { _: View -> aplicarFiltros() }
        checkRemedios.setOnClickListener(listenerFiltro)
        checkMedicos.setOnClickListener(listenerFiltro)
    }

    private fun carregarDadosGeral() {
        lifecycleScope.launch(Dispatchers.IO) {
            val localEntities = database.medicamentoDao().listarPorUsuario(usuarioId)
            val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())

            val listaLocal = localEntities.map { entity ->
                val dateAux = try { formatoHora.parse(entity.horario) } catch (e: Exception) { null }
                Medicamento(
                    id = entity.idRemoto ?: entity.idLocal,
                    nome_remedio = entity.nome,
                    dosagem = entity.dosagem,
                    quantidade_total = entity.quantidade_total,
                    horario_agendado = dateAux,
                    tomado = entity.tomado
                )
            }
            withContext(Dispatchers.Main) {
                listaMedicamentos = listaLocal
                aplicarFiltros()
            }
        }

        RetrofitClient.instance.getMedicamentos(usuarioId).enqueue(object : Callback<List<Medicamento>> {
            override fun onResponse(call: Call<List<Medicamento>>, response: Response<List<Medicamento>>) {
                if (response.isSuccessful) {
                    val listaOnline = response.body() ?: emptyList()
                    val formatoHoraStr = SimpleDateFormat("HH:mm", Locale.getDefault())

                    lifecycleScope.launch(Dispatchers.IO) {
                        val localEntitiesMap = database.medicamentoDao().listarPorUsuario(usuarioId).associateBy { it.idRemoto }

                        val entidades = listaOnline.map { med ->
                            val horaString = med.horario_agendado?.let { formatoHoraStr.format(it) } ?: "00:00"
                            val jaTomado = localEntitiesMap[med.id]?.tomado ?: false

                            MedicamentoEntity(
                                idRemoto = med.id,
                                nome = med.nome_remedio,
                                dosagem = med.dosagem,
                                horario = horaString,
                                usuarioId = usuarioId,
                                sincronizado = true,
                                quantidade_total = med.quantidade_total ?: 0,
                                tomado = jaTomado
                            )
                        }

                        database.medicamentoDao().limparTudo()
                        database.medicamentoDao().salvarVarios(entidades)

                        val novasEntities = database.medicamentoDao().listarPorUsuario(usuarioId)
                        val formatoData = SimpleDateFormat("HH:mm", Locale.getDefault())

                        withContext(Dispatchers.Main) {
                            listaMedicamentos = novasEntities.map { entity ->
                                val dAux = try { formatoData.parse(entity.horario) } catch (e: Exception) { null }
                                Medicamento(
                                    id = entity.idRemoto ?: entity.idLocal,
                                    nome_remedio = entity.nome,
                                    dosagem = entity.dosagem,
                                    quantidade_total = entity.quantidade_total,
                                    horario_agendado = dAux,
                                    tomado = entity.tomado
                                )
                            }
                            buscarConsultas()
                        }
                    }
                }
            }
            override fun onFailure(call: Call<List<Medicamento>>, t: Throwable) {
                buscarConsultas()
            }
        })
    }

    private fun buscarConsultas() {
        lifecycleScope.launch(Dispatchers.IO) {
            val localEntities = database.consultaDao().listarPorUsuario(usuarioId)
            val listaLocal = localEntities.map {
                ConsultaModel(
                    id = it.idRemoto ?: it.idLocal,
                    usuarioId = it.usuarioId,
                    medico = it.medico,
                    especialidade = it.especialidade,
                    dataHora = it.dataHora,
                    local = it.localConsulta,
                    realizada = it.realizada // 🔥 LÊ DO BANCO SE FOI!
                )
            }
            withContext(Dispatchers.Main) {
                listaConsultas = listaLocal
                aplicarFiltros()
            }
        }

        RetrofitClient.instance.listarConsultas(usuarioId).enqueue(object : Callback<List<ConsultaModel>> {
            override fun onResponse(call: Call<List<ConsultaModel>>, response: Response<List<ConsultaModel>>) {
                if (response.isSuccessful) {
                    val listaOnline = response.body() ?: emptyList()

                    lifecycleScope.launch(Dispatchers.IO) {
                        // Preserva a presença que está no banco local!
                        val localEntitiesMap = database.consultaDao().listarPorUsuario(usuarioId).associateBy { it.idRemoto }

                        val entidades = listaOnline.map { con ->
                            val jaFoi = localEntitiesMap[con.id]?.realizada ?: false

                            ConsultaEntity(
                                idRemoto = con.id,
                                medico = con.medico,
                                especialidade = con.especialidade,
                                dataHora = con.dataHora,
                                localConsulta = con.local ?: "Não informado",
                                usuarioId = usuarioId,
                                sincronizado = true,
                                realizada = jaFoi // 🔥 MANTÉM A PRESENÇA!
                            )
                        }
                        database.consultaDao().limparTudo()
                        database.consultaDao().salvarVarios(entidades)

                        val novasEntities = database.consultaDao().listarPorUsuario(usuarioId)
                        withContext(Dispatchers.Main) {
                            listaConsultas = novasEntities.map {
                                ConsultaModel(
                                    id = it.idRemoto ?: it.idLocal,
                                    usuarioId = it.usuarioId,
                                    medico = it.medico,
                                    especialidade = it.especialidade,
                                    dataHora = it.dataHora,
                                    local = it.localConsulta,
                                    realizada = it.realizada
                                )
                            }
                            aplicarFiltros()
                        }
                    }
                }
            }
            override fun onFailure(call: Call<List<ConsultaModel>>, t: Throwable) {}
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

        if (medico.isEmpty() || data.isEmpty() || hora.isEmpty()) return

        val dataHoraCompleta = "$data $hora"

        try {
            val formatoConsulta = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dataDaConsulta = formatoConsulta.parse(dataHoraCompleta)
            if (dataDaConsulta != null) {
                val tempoUmaHoraAntes = dataDaConsulta.time - (60 * 60 * 1000)

                if (tempoUmaHoraAntes > System.currentTimeMillis()) {
                    agendarNotificacaoAndroid(
                        id = (System.currentTimeMillis() % 10000).toInt(),
                        titulo = "Lembrete de Consulta! \uD83E\uDE7A",
                        mensagem = "Você tem consulta com $medico ($especialidade) em 1 hora!",
                        tempoEmMilisegundos = tempoUmaHoraAntes
                    )
                }
            }
        } catch (e: Exception) {}

        val novaConsulta = ConsultaEntity(
            medico = medico,
            especialidade = especialidade,
            dataHora = dataHoraCompleta,
            localConsulta = "Não informado",
            usuarioId = usuarioId,
            sincronizado = false,
            realizada = false // 🔥 NASCE COMO NÃO REALIZADA
        )

        lifecycleScope.launch(Dispatchers.IO) {
            database.consultaDao().salvar(novaConsulta)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@AgendaActivity, "Consulta Agendada e Alarme Ligado!", Toast.LENGTH_SHORT).show()
                limparCampos()
                buscarConsultas()

                val request = ConsultaModel(
                    usuarioId = usuarioId,
                    medico = medico,
                    especialidade = especialidade,
                    dataHora = dataHoraCompleta,
                    local = "Não informado"
                )

                RetrofitClient.instance.salvarConsulta(request).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        buscarConsultas()
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                })
            }
        }
    }

    private fun salvarRemedio() {
        val nome = editNomeRemedio.text.toString()
        val hora = editHoraInicio.text.toString()
        val qtdStr = editEstoque.text.toString()
        val qtd = qtdStr.toIntOrNull() ?: 0
        val dosagemTexto = editDosagem.text.toString()

        if (nome.isEmpty() || hora.isEmpty()) return

        val frequencia = when (radioFrequencia.checkedRadioButtonId) {
            R.id.rb8h -> 8
            R.id.rb12h -> 12
            R.id.rb24h -> 24
            else -> 24
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val listaLocal = mutableListOf<MedicamentoEntity>()
            val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dataBase = try { formatoHora.parse(hora) } catch (e: Exception) { java.util.Date() }

            val cal = Calendar.getInstance()
            if (dataBase != null) cal.time = dataBase

            val totalDoses = if (qtd > 0) qtd else 1

            for (i in 0 until totalDoses) {
                val horaFormatada = formatoHora.format(cal.time)

                listaLocal.add(
                    MedicamentoEntity(
                        nome = nome,
                        dosagem = dosagemTexto,
                        horario = horaFormatada,
                        usuarioId = usuarioId,
                        sincronizado = false,
                        quantidade_total = totalDoses - i,
                        tomado = false
                    )
                )

                agendarNotificacaoAndroid(
                    id = (System.currentTimeMillis() % 10000).toInt() + i,
                    titulo = "Hora do Remédio! \uD83D\uDC8A",
                    mensagem = "Está na hora de tomar: $nome ($dosagemTexto)",
                    tempoEmMilisegundos = cal.timeInMillis
                )
                cal.add(Calendar.HOUR_OF_DAY, frequencia)
            }

            database.medicamentoDao().salvarVarios(listaLocal)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@AgendaActivity, "Cronograma Gerado e Alarmes Ativados!", Toast.LENGTH_SHORT).show()
                limparCampos()
                carregarDadosGeral()

                val request = MedicamentoRequest(
                    usuario_id = usuarioId,
                    nome_remedio = nome,
                    dosagem = dosagemTexto,
                    horario_inicio = hora,
                    frequencia_horas = frequencia,
                    quantidade_total = qtd
                )

                RetrofitClient.instance.salvarMedicamento(request).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) { carregarDadosGeral() }
                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                })
            }
        }
    }

    private fun confirmarTomarRemedio(med: Medicamento) {
        if (!med.tomado) {
            AlertDialog.Builder(this)
                .setTitle("Confirmar Dose")
                .setMessage("Deseja marcar '${med.nome_remedio}' como tomado?\nO estoque será reduzido.")
                .setPositiveButton("Sim, tomei!") { _, _ -> atualizarStatusRemedio(med.id, true) }
                .setNegativeButton("Ainda não", null)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Desfazer Dose?")
                .setMessage("Deseja desmarcar esta dose?\n(O estoque no servidor não será devolvido automaticamente nesta versão).")
                .setPositiveButton("Desmarcar") { _, _ -> atualizarStatusRemedio(med.id, false) }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun atualizarStatusRemedio(id: Int, status: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.medicamentoDao().atualizarStatusTomado(id, status)
            withContext(Dispatchers.Main) { carregarDadosGeral() }
        }

        if (status) {
            RetrofitClient.instance.tomarMedicamento(id).enqueue(object : Callback<TomarMedicamentoResponse> {
                override fun onResponse(call: Call<TomarMedicamentoResponse>, response: Response<TomarMedicamentoResponse>) {}
                override fun onFailure(call: Call<TomarMedicamentoResponse>, t: Throwable) {}
            })
        }
    }

    // 🔥 NOVA LÓGICA DE PRESENÇA NO MÉDICO
    private fun confirmarPresencaConsulta(consulta: ConsultaModel) {
        if (!consulta.realizada) {
            AlertDialog.Builder(this)
                .setTitle("Confirmar Presença")
                .setMessage("Você compareceu à consulta com ${consulta.medico}?")
                .setPositiveButton("Sim, eu fui!") { _, _ ->
                    atualizarStatusConsulta(consulta.id ?: 0, true)
                }
                .setNegativeButton("Ainda não", null)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Desfazer Presença?")
                .setMessage("Deseja desmarcar a presença nesta consulta?")
                .setPositiveButton("Desmarcar") { _, _ ->
                    atualizarStatusConsulta(consulta.id ?: 0, false)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun atualizarStatusConsulta(id: Int, status: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.consultaDao().atualizarStatusRealizada(id, status)
            withContext(Dispatchers.Main) {
                buscarConsultas() // 🔥 Recarrega a tela de consultas
            }
        }
    }

    private fun excluirDose(id: Int) {
        val itemParaExcluir = listaMedicamentos.find { it.id == id }

        if (itemParaExcluir != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                database.medicamentoDao().deletar(id)
                withContext(Dispatchers.Main) { carregarDadosGeral() }
            }
            RetrofitClient.instance.excluirMedicamento(id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) { Toast.makeText(this@AgendaActivity, "Remédio removido!", Toast.LENGTH_SHORT).show() }
                override fun onFailure(call: Call<Void>, t: Throwable) {}
            })
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                database.consultaDao().deletar(id)
                withContext(Dispatchers.Main) { buscarConsultas() }
            }
            RetrofitClient.instance.deletarConsulta(id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) { Toast.makeText(this@AgendaActivity, "Consulta desmarcada!", Toast.LENGTH_SHORT).show() }
                override fun onFailure(call: Call<Void>, t: Throwable) {}
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

    private fun agendarNotificacaoAndroid(id: Int, titulo: String, mensagem: String, tempoEmMilisegundos: Long) {
        try {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = android.content.Intent(this, com.example.gerokernel.utils.LembreteReceiver::class.java).apply {
                putExtra("TITULO", titulo)
                putExtra("MENSAGEM", mensagem)
                putExtra("ID", id)
            }
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                this, id, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, tempoEmMilisegundos, pendingIntent)
        } catch (e: Exception) { }
    }
}