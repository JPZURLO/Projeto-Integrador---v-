package com.example.gerokernel.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gerokernel.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText

class SinaisVitaisActivity : AppCompatActivity() {

    private lateinit var chart: LineChart
    // Listas para guardar os dados temporariamente (Depois virá do Banco)
    private val entradasPressaoMax = ArrayList<Entry>()
    private val entradasGlicose = ArrayList<Entry>()
    private var contador = 0f // Simula o tempo (Dia 1, Dia 2...)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sinais_vitais)

        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltar)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val editSys = findViewById<TextInputEditText>(R.id.editPressaoSys)
        val editDia = findViewById<TextInputEditText>(R.id.editPressaoDia)
        val editGlicose = findViewById<TextInputEditText>(R.id.editGlicose)

        // Elementos de Feedback
        val cardFeedback = findViewById<MaterialCardView>(R.id.cardFeedback)
        val txtFeedback = findViewById<TextView>(R.id.txtMensagemFeedback)
        val iconFeedback = findViewById<ImageView>(R.id.iconFeedback)

        chart = findViewById(R.id.chartSinais)
        configurarGrafico()

        btnVoltar.setOnClickListener { finish() }

        btnRegistrar.setOnClickListener {
            val sysStr = editSys.text.toString()
            val diaStr = editDia.text.toString()
            val glicStr = editGlicose.text.toString()

            if (sysStr.isNotEmpty() && diaStr.isNotEmpty()) {
                val sys = sysStr.toFloat()
                val dia = diaStr.toFloat()

                // 1. Adicionar ao Gráfico
                adicionarDadosAoGrafico(sys, if(glicStr.isNotEmpty()) glicStr.toFloat() else 0f)

                // 2. Lógica do Semáforo (Feedback Visual)
                cardFeedback.visibility = View.VISIBLE

                if (sys > 140 || dia > 90) {
                    // ALERTA
                    cardFeedback.setCardBackgroundColor(Color.parseColor("#FFEBEE")) // Fundo Vermelho Claro
                    txtFeedback.text = "Atenção! Pressão alta detectada."
                    txtFeedback.setTextColor(Color.parseColor("#D32F2F"))
                    iconFeedback.setColorFilter(Color.parseColor("#D32F2F"))
                } else {
                    // NORMAL
                    cardFeedback.setCardBackgroundColor(Color.parseColor("#E8F5E9")) // Fundo Verde Claro
                    txtFeedback.text = "Sinais vitais dentro do normal."
                    txtFeedback.setTextColor(Color.parseColor("#2E7D32"))
                    iconFeedback.setColorFilter(Color.parseColor("#2E7D32"))
                }

                // Limpa campos
                editSys.text?.clear()
                editDia.text?.clear()
                editGlicose.text?.clear()

                // Esconde teclado (Opcional, boa prática)
                // hideKeyboard()
            } else {
                Toast.makeText(this, "Informe pelo menos a pressão.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarGrafico() {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(false) // Facilita para idoso não dar zoom sem querer

        // Remove grade do fundo para ficar limpo
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.axisLeft.setDrawGridLines(true)
        chart.axisRight.isEnabled = false
    }

    private fun adicionarDadosAoGrafico(pressaoMax: Float, glicose: Float) {
        contador++

        entradasPressaoMax.add(Entry(contador, pressaoMax))

        // Criar o Dataset da Pressão (Linha Vermelha)
        val setPressao = LineDataSet(entradasPressaoMax, "Pressão Máx")
        setPressao.color = Color.RED
        setPressao.lineWidth = 3f
        setPressao.setCircleColor(Color.RED)
        setPressao.circleRadius = 5f
        setPressao.setDrawValues(false)
        setPressao.mode = LineDataSet.Mode.CUBIC_BEZIER // Linha curva suave

        val dataSets = ArrayList<com.github.mikephil.charting.interfaces.datasets.ILineDataSet>()
        dataSets.add(setPressao)

        // Se tiver glicose, adiciona linha Azul
        if (glicose > 0) {
            entradasGlicose.add(Entry(contador, glicose))
            val setGlicose = LineDataSet(entradasGlicose, "Glicose")
            setGlicose.color = Color.BLUE
            setGlicose.lineWidth = 2f
            setGlicose.enableDashedLine(10f, 5f, 0f) // Linha tracejada para diferenciar
            dataSets.add(setGlicose)
        }

        val data = LineData(dataSets)
        chart.data = data
        chart.notifyDataSetChanged() // Avisa que mudou
        chart.invalidate() // Redesenha
        chart.animateX(500) // Animaçãozinha bonitinha
    }
}