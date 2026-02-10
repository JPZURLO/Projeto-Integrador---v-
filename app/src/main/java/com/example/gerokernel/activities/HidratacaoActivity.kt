package com.example.gerokernel.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gerokernel.R
import com.example.gerokernel.api.RetrofitClient
import com.example.gerokernel.models.HidratacaoRequest
import com.example.gerokernel.models.HidratacaoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class HidratacaoActivity : AppCompatActivity() {

    private var consumoAtual = 0
    private val metaDiaria = 2000

    // 🔥 MUDANÇA 1: Começa com 0
    private var usuarioId = 0

    // UI
    private lateinit var progressAgua: ProgressBar
    private lateinit var txtQuantidade: TextView
    private lateinit var txtMeta: TextView
    private lateinit var recyclerHistorico: RecyclerView
    private lateinit var adapter: HistoricoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidratacao)

        // 🔥 MUDANÇA 2: RECUPERAR O ID REAL QUE O LOGIN SALVOU
        // Atenção: O nome "SessaoUsuario" tem que ser IGUAL ao do LoginActivity
        val prefsLogin = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)
        usuarioId = prefsLogin.getInt("ID_USUARIO", 0)

        // Trava de Segurança: Se der 0, é porque não logou direito
        if (usuarioId == 0) {
            Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_LONG).show()
            finish() // Fecha a tela e volta
            return
        }

        // Recuperar o que já bebeu hoje (Visual Local)
        val sharedPref = getSharedPreferences("Hidratacao", Context.MODE_PRIVATE)
        consumoAtual = sharedPref.getInt("HOJE", 0)

        inicializarComponentes()
        atualizarInterface()
        carregarTabela()
    }

    private fun inicializarComponentes() {
        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltar)
        val btnCopo = findViewById<Button>(R.id.btnCopo)
        val btnGarrafa = findViewById<Button>(R.id.btnGarrafa)
        val btnZerar = findViewById<Button>(R.id.btnZerar)

        progressAgua = findViewById(R.id.progressAgua)
        txtQuantidade = findViewById(R.id.txtQuantidade)
        txtMeta = findViewById(R.id.txtMeta)
        recyclerHistorico = findViewById(R.id.recyclerHistorico)

        recyclerHistorico.layoutManager = LinearLayoutManager(this)
        adapter = HistoricoAdapter()
        recyclerHistorico.adapter = adapter

        btnVoltar.setOnClickListener { finish() }
        btnCopo.setOnClickListener { adicionarAgua(200) }
        btnGarrafa.setOnClickListener { adicionarAgua(500) }

        // Botão Zerar (Opcional)
        btnZerar.setOnClickListener {
            consumoAtual = 0
            val sharedPref = getSharedPreferences("Hidratacao", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putInt("HOJE", 0)
                apply()
            }
            atualizarInterface()
        }
    }

    private fun adicionarAgua(ml: Int) {
        // 1. Atualiza Visual (Rápido)
        consumoAtual += ml
        val sharedPref = getSharedPreferences("Hidratacao", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("HOJE", consumoAtual)
            apply()
        }
        atualizarInterface()

        // 2. Manda pro Servidor com o ID CERTO! ☁️
        val retrofit = RetrofitClient.instance
        val request = HidratacaoRequest(usuario_id = usuarioId, quantidade_ml = ml)

        retrofit.salvarHidratacao(request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    carregarTabela()
                } else {
                    Log.e("HIDRATACAO", "Erro API: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("HIDRATACAO", "Erro Rede: ${t.message}")
            }
        })

        if (consumoAtual >= metaDiaria) {
            Toast.makeText(this, "Meta batida! 🎉", Toast.LENGTH_SHORT).show()
        }
    }

    private fun carregarTabela() {
        val retrofit = RetrofitClient.instance
        retrofit.getHistoricoHidratacao(usuarioId).enqueue(object : Callback<List<HidratacaoResponse>> {
            override fun onResponse(call: Call<List<HidratacaoResponse>>, response: Response<List<HidratacaoResponse>>) {
                if (response.isSuccessful) {
                    val listaServidor = response.body() ?: emptyList()
                    processarDadosParaTabela(listaServidor)
                }
            }
            override fun onFailure(call: Call<List<HidratacaoResponse>>, t: Throwable) {
                Log.e("HIDRATACAO", "Falha ao carregar tabela")
            }
        })
    }

    private fun processarDadosParaTabela(listaBruta: List<HidratacaoResponse>) {
        val mapaPorDia = mutableMapOf<String, Int>()
        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatoSaida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        for (item in listaBruta) {
            try {
                val dataString = item.data_hora.take(10)
                val dataFormatada = try {
                    val dataObj = formatoEntrada.parse(dataString)
                    formatoSaida.format(dataObj!!)
                } catch (e: Exception) {
                    dataString
                }
                val totalAtual = mapaPorDia[dataFormatada] ?: 0
                mapaPorDia[dataFormatada] = totalAtual + item.quantidade_ml
            } catch (e: Exception) { continue }
        }

        val listaFinal = mapaPorDia.map { Pair(it.key, it.value) }.toList()
        adapter.atualizarLista(listaFinal)
    }

    private fun atualizarInterface() {
        progressAgua.max = metaDiaria
        progressAgua.progress = consumoAtual
        txtQuantidade.text = "$consumoAtual ml"
        val pct = (consumoAtual.toFloat() / metaDiaria.toFloat()) * 100
        txtMeta.text = "Meta: $metaDiaria ml (${pct.toInt()}%)"
    }

    class HistoricoAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<HistoricoAdapter.ViewHolder>() {

        private var dados: List<Pair<String, Int>> = emptyList()

        fun atualizarLista(novaLista: List<Pair<String, Int>>) {
            dados = novaLista
            notifyDataSetChanged()
        }

        class ViewHolder(view: android.view.View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val txtData: android.widget.TextView = view.findViewById(R.id.txtDataHistorico)
            val txtMl: android.widget.TextView = view.findViewById(R.id.txtMlHistorico)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_historico, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = dados[position]
            holder.txtData.text = item.first
            holder.txtMl.text = "${item.second} ml"
        }

        override fun getItemCount() = dados.size
    }
}