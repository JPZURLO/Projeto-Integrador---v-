package com.example.gerokernel.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gerokernel.R
import com.example.gerokernel.api.RetrofitClient
import com.exemplo.gerokernel.model.LoginRequest
import com.exemplo.gerokernel.api.ApiService
import com.exemplo.gerokernel.models.User
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Vincular os componentes do XML novo
        val editEmail = findViewById<TextInputEditText>(R.id.editLoginEmail)
        val editSenha = findViewById<TextInputEditText>(R.id.editLoginSenha)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)
        val btnIrCadastro = findViewById<Button>(R.id.btnIrCadastro) // Botão de texto no rodapé
        val txtEsqueciSenha = findViewById<TextView>(R.id.txtEsqueciSenha) // Link de recuperar senha
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // 2. Ação: Ir para a tela de Cadastro
        btnIrCadastro.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        txtEsqueciSenha.setOnClickListener {
            val intent = Intent(this, RecuperarSenhaActivity::class.java)
            startActivity(intent)
        }

        // 4. Ação: Botão ENTRAR
        btnEntrar.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val senha = editSenha.text.toString().trim()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                // UX: Mostra loading e esconde botão para evitar duplo clique
                progressBar.visibility = View.VISIBLE
                btnEntrar.isEnabled = false
                btnEntrar.text = "Carregando..."

                // Objeto de envio
                val loginRequest = ApiService.LoginRequest(email, senha)

                // Chamada à API (MySQL/Node.js)
                RetrofitClient.instance.fazerLogin(loginRequest).enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        progressBar.visibility = View.GONE
                        btnEntrar.isEnabled = true
                        btnEntrar.text = "ENTRAR"

                        if (response.isSuccessful && response.body() != null) {
                            val usuarioLogado = response.body()!!
                            Toast.makeText(this@LoginActivity, "Bem-vindo, ${usuarioLogado.nome}!", Toast.LENGTH_LONG).show()

                            // SUCESSO: Vai para a tela Principal (Home)
                            // val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            // startActivity(intent)
                            // finish()

                        } else {
                            Toast.makeText(this@LoginActivity, "E-mail ou senha inválidos.", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        btnEntrar.isEnabled = true
                        btnEntrar.text = "ENTRAR"

                        Log.e("API_LOGIN", "Erro: ${t.message}")
                        Toast.makeText(this@LoginActivity, "Erro de conexão. Verifique a internet.", Toast.LENGTH_LONG).show()
                    }
                })
            } else {
                Toast.makeText(this, "Preencha e-mail e senha!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}