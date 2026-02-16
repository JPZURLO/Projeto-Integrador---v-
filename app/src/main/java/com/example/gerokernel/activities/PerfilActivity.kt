package com.example.gerokernel.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gerokernel.R
import com.example.gerokernel.api.RetrofitClient
import com.example.gerokernel.models.AtualizarPerfilRequest
import com.example.gerokernel.models.FichaMedicaResponse
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PerfilActivity : AppCompatActivity() {

    private var usuarioId = 0

    // Campos
    private lateinit var editNome: TextInputEditText
    private lateinit var editEmail: TextInputEditText
    private lateinit var editTelefone: TextInputEditText
    private lateinit var editSangue: TextInputEditText
    private lateinit var editAlergias: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // 1. Pega ID da Sessão
        val prefs = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)
        usuarioId = prefs.getInt("ID_USUARIO", 0)
        val nomeAtual = prefs.getString("NOME_USUARIO", "")

        // 2. Vincula componentes
        editNome = findViewById(R.id.editNome)
        editEmail = findViewById(R.id.editEmail)
        editTelefone = findViewById(R.id.editTelefone)
        editSangue = findViewById(R.id.editSangue)
        editAlergias = findViewById(R.id.editAlergias)

        val btnSalvar = findViewById<Button>(R.id.btnSalvar)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val imgPerfil = findViewById<ImageView>(R.id.imgPerfil)

        // Preenche o nome que já temos localmente
        editNome.setText(nomeAtual)

        // 3. Busca dados completos no servidor
        carregarDados()

        // 4. Ação Salvar
        btnSalvar.setOnClickListener { salvarAlteracoes() }

        // 5. Ação Logout
        btnLogout.setOnClickListener { fazerLogout() }

        // 6. Ação Foto (Simulação)
        imgPerfil.setOnClickListener {
            Toast.makeText(this, "Galeria de fotos em breve!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun carregarDados() {
        // Aproveitamos a rota da Ficha Médica que traz quase tudo
        RetrofitClient.instance.getFichaMedica(usuarioId).enqueue(object : Callback<FichaMedicaResponse> {
            override fun onResponse(call: Call<FichaMedicaResponse>, response: Response<FichaMedicaResponse>) {
                if (response.isSuccessful) {
                    val dados = response.body()
                    editTelefone.setText(dados?.contato_telefone)
                    editSangue.setText(dados?.tipo_sanguineo)
                    editAlergias.setText(dados?.alergias)
                    // Nota: O email precisaria vir de outra rota se quisermos exibir,
                    // ou salvamos no SharedPreferences no Login também.
                }
            }
            override fun onFailure(call: Call<FichaMedicaResponse>, t: Throwable) {
                // Erro silencioso ou aviso
            }
        })
    }

    private fun salvarAlteracoes() {
        val request = AtualizarPerfilRequest(
            nome = editNome.text.toString(),
            email = editEmail.text.toString(),
            telefone = editTelefone.text.toString(),
            tipo_sanguineo = editSangue.text.toString(),
            alergias = editAlergias.text.toString()
        )

        RetrofitClient.instance.atualizarPerfil(usuarioId, request).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(applicationContext, "Perfil atualizado! ✅", Toast.LENGTH_SHORT).show()

                    // Atualiza o nome na sessão local também
                    val prefs = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)
                    with(prefs.edit()) {
                        putString("NOME_USUARIO", request.nome)
                        apply()
                    }
                    finish() // Volta pra Home
                } else {
                    Toast.makeText(applicationContext, "Erro ao salvar.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) { // Use Throwable!
                android.util.Log.e("PERFIL_DEBUG", "Erro: ${t.message}")
                Toast.makeText(applicationContext, "Erro de conexão.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fazerLogout() {
        // Limpa TUDO do SharedPreferences
        val prefs = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Volta pra tela de Login e limpa a pilha de telas (não deixa voltar com botão 'voltar')
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}