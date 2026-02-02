package com.example.gerokernel.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gerokernel.R
import com.google.android.material.textfield.TextInputEditText

class RedefinirSenhaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_redefinir_senha)

        val editEmail = findViewById<TextInputEditText>(R.id.editEmailBloqueado)
        val editNovaSenha = findViewById<TextInputEditText>(R.id.editNovaSenha)
        val btnSalvar = findViewById<Button>(R.id.btnSalvarSenha)

        // 1. Capturar os dados do LINK (Deep Link)
        // O link será algo como: gerokernel://redefinir?email=joao@teste.com
        val data: Uri? = intent.data
        if (data != null) {
            val emailDoLink = data.getQueryParameter("email")
            editEmail.setText(emailDoLink)
        } else {
            // Fallback para testes manuais
            editEmail.setText(intent.getStringExtra("email_extra") ?: "erro@email.com")
        }

        // 2. Salvar Senha
        btnSalvar.setOnClickListener {
            val novaSenha = editNovaSenha.text.toString()
            val emailAlvo = editEmail.text.toString()

            if (novaSenha.isNotEmpty()) {
                // AQUI ENTRA O RETROFIT PARA O NODE.JS (UPDATE users SET senha = ...)
                Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_LONG).show()

                // Volta para o Login
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Limpa a pilha para não voltar aqui
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Digite a nova senha.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}