package com.example.gerokernel.activities

import android.content.Context
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
import com.example.gerokernel.api.ApiService// Ajustei o pacote
import com.example.gerokernel.model.User // Ajustei o pacote
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Vincular os componentes
        val editEmail = findViewById<TextInputEditText>(R.id.editLoginEmail)
        val editSenha = findViewById<TextInputEditText>(R.id.editLoginSenha)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)
        val btnIrCadastro = findViewById<Button>(R.id.btnIrCadastro)
        val txtEsqueciSenha = findViewById<TextView>(R.id.txtEsqueciSenha)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // 2. Ação: Ir para Cadastro
        btnIrCadastro.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        // 3. Ação: Esqueci a Senha
        txtEsqueciSenha.setOnClickListener {
            val intent = Intent(this, RecuperarSenhaActivity::class.java)
            startActivity(intent)
        }

        // 4. Ação: Botão ENTRAR
        btnEntrar.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val senha = editSenha.text.toString().trim()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                // UX: Loading
                progressBar.visibility = View.VISIBLE
                btnEntrar.isEnabled = false
                btnEntrar.text = "Carregando..."

                // Objeto de envio (Verifique se LoginRequest está dentro de ApiService ou Models)
                // Se der erro aqui, certifique-se de onde está a classe LoginRequest
                val loginRequest = ApiService.LoginRequest(email, senha)

                // Chamada à API
                RetrofitClient.instance.fazerLogin(loginRequest).enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        progressBar.visibility = View.GONE
                        btnEntrar.isEnabled = true
                        btnEntrar.text = "ENTRAR"

                        if (response.isSuccessful && response.body() != null) {
                            val usuarioLogado = response.body()!!

                            val sharedPref = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)
                            with (sharedPref.edit()) {
                                putInt("ID_USUARIO", usuarioLogado.id) // Fundamental para os Sinais Vitais!
                                putString("NOME_USUARIO", usuarioLogado.nome)
                                apply() // Salva de verdade
                            }
                            // ====================================================

                            Toast.makeText(this@LoginActivity, "Bem-vindo, ${usuarioLogado.nome}!", Toast.LENGTH_LONG).show()

                            // Navegação
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("NOME_USUARIO", usuarioLogado.nome)
                            startActivity(intent)
                            finish()

                        } else {
                            Toast.makeText(this@LoginActivity, "E-mail ou senha inválidos.", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        btnEntrar.isEnabled = true
                        btnEntrar.text = "ENTRAR"
                        Log.e("API_LOGIN", "Erro: ${t.message}")
                        Toast.makeText(this@LoginActivity, "Erro de conexão: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            } else {
                Toast.makeText(this, "Preencha e-mail e senha!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}