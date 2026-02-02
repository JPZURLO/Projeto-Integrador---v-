package com.example.gerokernel.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gerokernel.R
import com.example.gerokernel.api.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecuperarSenhaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_senha)

        val btnVoltar = findViewById<ImageButton>(R.id.btnVoltar)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmailRecuperacao)
        val btnEnviar = findViewById<Button>(R.id.btnEnviarEmail)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarRecuperacao)

        btnVoltar.setOnClickListener { finish() }

        btnEnviar.setOnClickListener {
            val email = editEmail.text.toString().trim()

            if (email.isNotEmpty()) {
                // 1. Muda visual para "Carregando"
                progressBar.visibility = View.VISIBLE
                btnEnviar.isEnabled = false
                btnEnviar.text = "ENVIANDO..."

                // 2. Prepara o JSON para enviar { "email": "..." }
                val dadosEnvio = mapOf("email" to email)

                // 3. CHAMADA REAL PARA A API (Adeus Simulação!)
                RetrofitClient.instance.solicitarRecuperacao(dadosEnvio).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        // Voltar botão ao normal
                        progressBar.visibility = View.GONE
                        btnEnviar.isEnabled = true
                        btnEnviar.text = "ENVIAR LINK"

                        if (response.isSuccessful) {
                            Toast.makeText(this@RecuperarSenhaActivity, "Link enviado! Verifique seu e-mail.", Toast.LENGTH_LONG).show()
                            finish() // Fecha a tela pois deu certo
                        } else {
                            // Se o e-mail não existir no banco (404) ou erro 500
                            Toast.makeText(this@RecuperarSenhaActivity, "E-mail não encontrado ou erro no servidor.", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        btnEnviar.isEnabled = true
                        btnEnviar.text = "ENVIAR LINK"

                        Toast.makeText(this@RecuperarSenhaActivity, "Falha na conexão: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })

            } else {
                Toast.makeText(this, "Por favor, digite seu e-mail.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}