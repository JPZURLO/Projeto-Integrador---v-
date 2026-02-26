package com.example.gerokernel.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gerokernel.R
import com.example.gerokernel.adapters.SinaisAdapter
import com.example.gerokernel.api.RetrofitClient
import com.example.gerokernel.models.SinaisModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SinaisVitaisActivity : AppCompatActivity() {

    private lateinit var recyclerHistorico: RecyclerView
    private var usuarioIdLogado: Int = 0

    private var listaCompletaBackup: List<SinaisModel> = ArrayList()
    private var listaAtualFiltrada: List<SinaisModel> = ArrayList()

    // UI Components
    private lateinit var cardFeedback: MaterialCardView
    private lateinit var txtFeedback: TextView
    private lateinit var iconFeedback: ImageView
    private lateinit var txtResumoTexto: TextView
    private lateinit var txtDicaDoDia: TextView
    private lateinit var cardResumoDia: MaterialCardView

    private lateinit var btn7Dias: MaterialButton
    private lateinit var btn30Dias: MaterialButton
    private lateinit var btnTodos: MaterialButton
    private lateinit var btnPdf: MaterialButton

    private val createPdfLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> escreverPDFnaUri(uri) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sinais_vitais)

        val sharedPref = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)
        usuarioIdLogado = sharedPref.getInt("ID_USUARIO", 0)

        inicializarComponentes()
        carregarHistorico()

        btn7Dias.setOnClickListener { filtrarPorData(7) }
        btn30Dias.setOnClickListener { filtrarPorData(30) }
        btnTodos.setOnClickListener { filtrarPorData(9999) }

        btnPdf.setOnClickListener {
            if (listaAtualFiltrada.isNotEmpty()) abrirSeletorDeArquivo()
            else Toast.makeText(this, "Sem dados para gerar PDF.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun inicializarComponentes() {
        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltar)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val editSys = findViewById<TextInputEditText>(R.id.editPressaoSys)
        val editDia = findViewById<TextInputEditText>(R.id.editPressaoDia)
        val editGlicose = findViewById<TextInputEditText>(R.id.editGlicose)

        cardFeedback = findViewById(R.id.cardFeedback)
        txtFeedback = findViewById(R.id.txtMensagemFeedback)
        txtResumoTexto = findViewById(R.id.txtResumoTexto)
        txtDicaDoDia = findViewById(R.id.txtDicaDoDia)
        cardResumoDia = findViewById(R.id.cardResumoDia)

        recyclerHistorico = findViewById(R.id.recyclerHistorico)
        recyclerHistorico.layoutManager = LinearLayoutManager(this)

        btn7Dias = findViewById(R.id.btnFiltro7)
        btn30Dias = findViewById(R.id.btnFiltro30)
        btnTodos = findViewById(R.id.btnFiltroTodos)
        btnPdf = findViewById(R.id.btnExportarPdf)

        btnVoltar.setOnClickListener { finish() }

        btnRegistrar.setOnClickListener {
            val sysStr = editSys.text.toString()
            val diaStr = editDia.text.toString()
            val glicStr = editGlicose.text.toString()

            if (sysStr.isNotEmpty() && diaStr.isNotEmpty()) {
                val sys = sysStr.toInt()
                val dia = diaStr.toInt()
                val glic = if (glicStr.isNotEmpty()) glicStr.toInt() else null

                atualizarFeedbackVisual(sys, dia)
                salvarNoBanco(sys, dia, glic)
                editSys.text?.clear()
                editDia.text?.clear()
                editGlicose.text?.clear()
            } else {
                Toast.makeText(this, "Informe a pressão.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // =========================================================================
    // LÓGICA DE PDF (GRÁFICO INVISÍVEL + TABELA)
    // =========================================================================

    private fun gerarBitmapDoGraficoParaPDF(lista: List<SinaisModel>): android.graphics.Bitmap? {
        if (lista.isEmpty()) return null

        // Cria gráfico em memória (invisível para o usuário)
        val chartPDF = LineChart(this)
        val width = 600
        val height = 400

        // Força o Android a desenhar o gráfico na memória
        chartPDF.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        chartPDF.layout(0, 0, width, height)

        val entriesSys = ArrayList<Entry>()
        val entriesGlic = ArrayList<Entry>()

        lista.forEachIndexed { i, s ->
            entriesSys.add(Entry(i.toFloat(), s.sistolica.toFloat()))
            if (s.glicose != null && s.glicose > 0) entriesGlic.add(Entry(i.toFloat(), s.glicose.toFloat()))
        }

        val sets = ArrayList<com.github.mikephil.charting.interfaces.datasets.ILineDataSet>()
        val setSys = LineDataSet(entriesSys, "Pressão").apply {
            color = Color.RED; lineWidth = 3f; setCircleColor(Color.RED); setDrawValues(false)
        }
        sets.add(setSys)

        if (entriesGlic.isNotEmpty()) {
            sets.add(LineDataSet(entriesGlic, "Glicose").apply { color = Color.BLUE; lineWidth = 2f; enableDashedLine(10f, 10f, 0f) })
        }

        chartPDF.data = LineData(sets)
        chartPDF.description.isEnabled = false
        chartPDF.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chartPDF.axisRight.isEnabled = false

        return chartPDF.chartBitmap
    }

    private fun escreverPDFnaUri(uri: Uri) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        var yAtual = 50f

        // 1. CABEÇALHO
        paint.textSize = 24f; paint.isFakeBoldText = true; paint.color = Color.BLACK
        canvas.drawText("Relatório GeroKernel", 40f, yAtual, paint)
        yAtual += 30f

        paint.textSize = 14f; paint.isFakeBoldText = false; paint.color = Color.DKGRAY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val hoje = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            canvas.drawText("Emitido em: $hoje", 40f, yAtual, paint)
        }
        yAtual += 40f

        // 2. GRÁFICO (Aqui o médico vê as linhas de tendência)
        try {
            val bitmap = gerarBitmapDoGraficoParaPDF(listaAtualFiltrada)
            if (bitmap != null) {
                val largura = 500
                val escala = largura.toFloat() / bitmap.width
                val altura = (bitmap.height * escala).toInt()
                val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, largura, altura, true)
                canvas.drawBitmap(scaled, 40f, yAtual, paint)
                yAtual += altura + 30f
            }
        } catch (e: Exception) {
            Log.e("PDF", "Erro grafico: ${e.message}")
        }

        // 3. TABELA (Cabeçalho)
        if (yAtual > 750) yAtual = 50f
        paint.textSize = 14f; paint.isFakeBoldText = true; paint.color = Color.parseColor("#1565C0")
        canvas.drawText("Data", 40f, yAtual, paint)
        canvas.drawText("Pressão", 150f, yAtual, paint)
        canvas.drawText("Classificação", 280f, yAtual, paint)

        paint.color = Color.LTGRAY; paint.strokeWidth = 2f
        canvas.drawLine(40f, yAtual + 10f, 550f, yAtual + 10f, paint)
        yAtual += 30f

        // 4. DADOS DA TABELA (Aqui mantemos 120/80 para o médico!)
        paint.textSize = 12f; paint.isFakeBoldText = false; paint.color = Color.BLACK
        listaAtualFiltrada.forEach { item ->
            var dataF = "-"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && item.data_hora != null) {
                try { dataF = LocalDate.parse(item.data_hora.substring(0, 10)).format(DateTimeFormatter.ofPattern("dd/MM")) } catch (e: Exception){}
            }

            canvas.drawText(dataF, 40f, yAtual, paint)
            // MÉDICO GOSTA DE mmHg (120/80), NÃO CONVERTEMOS AQUI
            canvas.drawText("${item.sistolica}/${item.diastolica}", 150f, yAtual, paint)

            val classif = obterClassificacao(item.sistolica, item.diastolica)
            paint.color = classif.iconeCor
            val textoLimpo = classif.texto.replace(Regex("[^\\p{L}\\p{N}\\s\\(\\)]"), "").trim()
            canvas.drawText(textoLimpo, 280f, yAtual, paint)

            paint.color = Color.BLACK
            yAtual += 25f
            if (yAtual > 800) return@forEach
        }

        pdfDocument.finishPage(page)

        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            Toast.makeText(this, "Relatório Salvo!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao salvar PDF", Toast.LENGTH_SHORT).show()
        }
        pdfDocument.close()
    }

    private fun abrirSeletorDeArquivo() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            val nomeArquivo = "Relatorio_Saude_${System.currentTimeMillis()}.pdf"
            putExtra(Intent.EXTRA_TITLE, nomeArquivo)
        }
        createPdfLauncher.launch(intent)
    }

    // =========================================================================
    // LÓGICA DE DADOS, FILTROS E CLASSIFICAÇÃO
    // =========================================================================

    private fun filtrarPorData(dias: Int) {
        atualizarEstiloBotoes(dias)
        if (dias == 9999) {
            listaAtualFiltrada = listaCompletaBackup
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val limite = LocalDate.now().minusDays(dias.toLong())
            listaAtualFiltrada = listaCompletaBackup.filter {
                try { LocalDate.parse(it.data_hora?.substring(0, 10)).let { d -> d.isAfter(limite) || d.isEqual(limite) } } catch(e:Exception){true}
            }
        }

        recyclerHistorico.adapter = SinaisAdapter(listaAtualFiltrada)
        gerarResumoDoPeriodo(listaAtualFiltrada)
    }

    private fun carregarHistorico() {
        RetrofitClient.instance.listarSinais(usuarioIdLogado).enqueue(object : Callback<List<SinaisModel>> {
            override fun onResponse(call: Call<List<SinaisModel>>, r: Response<List<SinaisModel>>) {
                if (r.isSuccessful) { listaCompletaBackup = r.body()!!; filtrarPorData(9999) }
            }
            override fun onFailure(call: Call<List<SinaisModel>>, t: Throwable) {}
        })
    }

    private fun salvarNoBanco(sys: Int, dia: Int, glic: Int?) {
        val m = SinaisModel(usuarioId = usuarioIdLogado, sistolica = sys, diastolica = dia, glicose = glic)
        RetrofitClient.instance.salvarSinais(m).enqueue(object : Callback<SinaisModel> {
            override fun onResponse(call: Call<SinaisModel>, r: Response<SinaisModel>) { if(r.isSuccessful) carregarHistorico() }
            override fun onFailure(call: Call<SinaisModel>, t: Throwable) {}
        })
    }

    data class ClassificacaoPressao(val texto: String, val corFundo: String, val corTexto: String, val iconeCor: Int)

    private fun obterClassificacao(sys: Int, dia: Int): ClassificacaoPressao {
        if (sys >= 140 && dia < 90) return ClassificacaoPressao("Hipertensão Sistólica Isolada ⚠️", "#FFF3E0", "#E65100", Color.parseColor("#E65100"))
        if (sys >= 180 || dia >= 110) return ClassificacaoPressao("Hipertensão Grave (Estágio 3) 🚨", "#FFEBEE", "#D32F2F", Color.RED)
        if (sys >= 160 || dia >= 100) return ClassificacaoPressao("Hipertensão Moderada (Estágio 2) ⚠️", "#FFF3E0", "#E65100", Color.parseColor("#E65100"))
        if (sys >= 140 || dia >= 90) return ClassificacaoPressao("Hipertensão Leve (Estágio 1) ⚠️", "#FFF8E1", "#F57F17", Color.parseColor("#F57F17"))
        if (sys >= 130 || dia >= 85) return ClassificacaoPressao("Normal Limítrofe 👁️", "#F1F8E9", "#827717", Color.parseColor("#827717"))
        if (sys < 130 && dia < 85) return ClassificacaoPressao("Normal ✅", "#E8F5E9", "#2E7D32", Color.parseColor("#2E7D32"))
        return ClassificacaoPressao("Analisando...", "#FFFFFF", "#000000", Color.BLACK)
    }

    private fun atualizarFeedbackVisual(sys: Int, dia: Int) {
        val resultado = obterClassificacao(sys, dia)

        // === NOVA LÓGICA HÍBRIDA TAMBÉM AQUI ===
        fun fmt(v: Int): String {
            if (v < 50) return v.toString() // Se digitou 12, mostra 12
            return if (v % 10 == 0) (v / 10).toString() else (v / 10.0).toString() // Se 120, vira 12
        }

        cardFeedback.visibility = View.VISIBLE
        cardFeedback.setCardBackgroundColor(Color.parseColor(resultado.corFundo))

        txtFeedback.text = "${fmt(sys)}/${fmt(dia)} - ${resultado.texto}"

        txtFeedback.setTextColor(Color.parseColor(resultado.corTexto))
        iconFeedback.setColorFilter(resultado.iconeCor)

        // Dica: Ajustei o toast para só apitar se for conversão alta
        if (sys >= 140 || (sys < 50 && sys >= 14)) {
            // Lógica simples: se for >140 (mmHg) ou >14 (popular) é alto
            Toast.makeText(this, "Atenção: Pressão Alta", Toast.LENGTH_SHORT).show()
        }
    }

    private fun gerarResumoDoPeriodo(l: List<SinaisModel>) {
        if(l.isEmpty()) { txtResumoTexto.text="Sem dados"; cardResumoDia.setCardBackgroundColor(Color.WHITE); return }
        val ultimo = l.last()
        val classif = obterClassificacao(ultimo.sistolica, ultimo.diastolica)
        txtResumoTexto.text = classif.texto
        txtResumoTexto.setTextColor(Color.parseColor(classif.corTexto))
        cardResumoDia.setCardBackgroundColor(Color.parseColor(classif.corFundo))
        txtDicaDoDia.text = "Consenso Brasileiro de HA."
    }

    private fun atualizarEstiloBotoes(diasAtivo: Int) {
        val corFundoPadrao = Color.TRANSPARENT
        fun pintar(btn: MaterialButton, diasDoBotao: Int) {
            if (diasDoBotao == diasAtivo) {
                btn.setBackgroundColor(Color.parseColor("#2E7D32"))
                btn.setTextColor(Color.WHITE)
            } else {
                btn.setBackgroundColor(corFundoPadrao)
                btn.setTextColor(Color.parseColor("#2E7D32"))
                btn.strokeColor = ColorStateList.valueOf(Color.parseColor("#2E7D32"))
                btn.strokeWidth = 2
            }
        }
        pintar(btn7Dias, 7); pintar(btn30Dias, 30); pintar(btnTodos, 9999)
    }
}