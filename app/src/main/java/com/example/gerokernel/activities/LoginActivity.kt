package com.example.gerokernel.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gerokernel.R
import com.example.gerokernel.api.RetrofitClient
import com.example.gerokernel.model.User
import com.example.gerokernel.model.LoginRequest
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Lógica de "Manter Conectado": Verifica ANTES de carregar o layout
        val sharedPref = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)
        val manterConectado = sharedPref.getBoolean("MANTER_CONECTADO", false)

        if (manterConectado) {
            val intent = Intent(this, MainActivity::class.java)
            val nome = sharedPref.getString("NOME_USUARIO", "")
            intent.putExtra("NOME_USUARIO", nome)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        // 2. Vincular os componentes
        val editEmail = findViewById<TextInputEditText>(R.id.editLoginEmail)
        val editSenha = findViewById<TextInputEditText>(R.id.editLoginSenha)
        val checkManter = findViewById<CheckBox>(R.id.checkManterConectado)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)
        val btnIrCadastro = findViewById<Button>(R.id.btnIrCadastro)
        val txtEsqueciSenha = findViewById<TextView>(R.id.txtEsqueciSenha)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // 3. Navegação
        btnIrCadastro.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }

        txtEsqueciSenha.setOnClickListener {
            startActivity(Intent(this, RecuperarSenhaActivity::class.java))
        }

        // 4. Ação de Login
        btnEntrar.setOnClickListener {
            val email = editEmail.text.toString().trim()
            val senha = editSenha.text.toString().trim()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                btnEntrar.isEnabled = false

                val loginRequest = LoginRequest(email, senha)

                RetrofitClient.instance.fazerLogin(loginRequest).enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        progressBar.visibility = View.GONE
                        btnEntrar.isEnabled = true

                        if (response.isSuccessful) {
                            val user = response.body()
                            if (user != null) {
                                // --- O PULO DO GATO: SALVAR PARA USO OFFLINE ---
                                val editor = sharedPref.edit()
                                editor.putString("EMAIL_USUARIO", email)
                                editor.putString("SENHA_USUARIO", senha) // Guardamos para comparar offline
                                editor.putString("NOME_USUARIO", user.nome)
                                editor.putInt("ID_USUARIO", user.id)
                                editor.apply()

                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.putExtra("NOME_USUARIO", user.nome)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "E-mail ou senha incorretos!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        btnEntrar.isEnabled = true

                        // --- LÓGICA DE LOGIN OFFLINE ---
                        val emailSalvo = sharedPref.getString("EMAIL_USUARIO", null)
                        val senhaSalva = sharedPref.getString("SENHA_USUARIO", null)

                        if (email == emailSalvo && senha == senhaSalva) {
                            Toast.makeText(this@LoginActivity, "Modo Offline: Bem-vindo de volta!", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("NOME_USUARIO", sharedPref.getString("NOME_USUARIO", "Usuário"))
                            startActivity(intent)
                            finish()
                        } else {
                            Log.e("API_LOGIN", "Erro: ${t.message}")
                            Toast.makeText(this@LoginActivity, "Sem conexão. Tente um login online primeiro.", Toast.LENGTH_LONG).show()
                        }
                    }
                })
            } else {
                Toast.makeText(this, "Preencha e-mail e senha!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}