package com.example.gerokernel.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gerokernel.R
import com.example.gerokernel.api.RetrofitClient
import com.example.gerokernel.models.FichaMedicaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmergenciaActivity : AppCompatActivity() {

    private lateinit var txtTipoSanguineo: TextView
    private lateinit var txtAlergias: TextView
    private lateinit var txtRemedios: TextView
    private var usuarioId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergencia)

        // 1. Recuperar o ID do usuário logado
        val prefs = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)
        usuarioId = prefs.getInt("ID_USUARIO", 0)

        // 2. Inicializar componentes da tela
        txtTipoSanguineo = findViewById(R.id.txtTipoSanguineo)
        txtAlergias = findViewById(R.id.txtAlergias)
        txtRemedios = findViewById(R.id.txtRemedios)
        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltar)

        btnVoltar.setOnClickListener { finish() }

        // 3. Carregar os dados do servidor
        buscarDadosFicha()
    }

    private fun buscarDadosFicha() {
        RetrofitClient.instance.getFicha(usuarioId).enqueue(object : Callback<FichaMedicaResponse> {
            override fun onResponse(call: Call<FichaMedicaResponse>, response: Response<FichaMedicaResponse>) {
                if (response.isSuccessful) {
                    val ficha = response.body()

                    // Preencher campos básicos
                    txtTipoSanguineo.text = ficha?.tipo_sanguineo ?: "Não informado"
                    txtAlergias.text = ficha?.alergias ?: "Nenhuma alergia relatada"

                    // Lógica para listar remédios e estoque
                    val lista = ficha?.lista_medicamentos ?: emptyList()
                    val resumoRemedios = StringBuilder()

                    if (lista.isEmpty()) {
                        resumoRemedios.append("Nenhum medicamento de uso contínuo cadastrado.")
                    } else {
                        lista.forEach { med ->
                            resumoRemedios.append("💊 ${med.nome_remedio}\n")
                            resumoRemedios.append("Dose: ${med.dosagem}\n")
                            resumoRemedios.append("Estoque: ${med.quantidade_total} unidades\n")

                            // Alerta visual se o estoque estiver baixo
                            if ((med.quantidade_total ?: 0) <= 5) {
                                resumoRemedios.append("⚠️ ESTOQUE CRÍTICO!\n")
                            }
                            resumoRemedios.append("\n")
                        }
                    }

                    txtRemedios.text = resumoRemedios.toString()

                } else {
                    Log.e("FICHA_DEBUG", "Erro: ${response.code()}")
                    Toast.makeText(this@EmergenciaActivity, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<FichaMedicaResponse>, t: Throwable) {
                Log.e("FICHA_DEBUG", "Falha crítica: ${t.message}")
                Toast.makeText(this@EmergenciaActivity, "Sem conexão com o servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }
}