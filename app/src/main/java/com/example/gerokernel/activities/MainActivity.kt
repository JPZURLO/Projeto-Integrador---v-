package com.example.gerokernel.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gerokernel.R
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Personalizar Saudação
        // Pegamos o nome que veio do LoginActivity
        val txtOla = findViewById<TextView>(R.id.txtOla)
        val nomeUsuario = intent.getStringExtra("NOME_USUARIO") ?: "Usuário"
        txtOla.text = "Olá, $nomeUsuario"

        // 2. Mapear os Cards
        val cardSinais = findViewById<MaterialCardView>(R.id.cardSinais)
        val cardAgenda = findViewById<MaterialCardView>(R.id.cardAgenda)
        val cardAgua = findViewById<MaterialCardView>(R.id.cardAgua)
        val cardEmergencia = findViewById<MaterialCardView>(R.id.cardEmergencia)

        // 3. Configurar Cliques

        // === AQUI ESTAVA FALTANDO! ===
        cardSinais.setOnClickListener {
            // Agora sim: Navega para a tela de Sinais Vitais
            val intent = Intent(this, SinaisVitaisActivity::class.java)
            startActivity(intent)
        }
        // ==============================

        cardAgenda.setOnClickListener {
            Toast.makeText(this, "Agenda de Consultas em breve!", Toast.LENGTH_SHORT).show()
        }

        cardAgua.setOnClickListener {
            Toast.makeText(this, "Bot de Hidratação em breve!", Toast.LENGTH_SHORT).show()
        }

        cardEmergencia.setOnClickListener {
            Toast.makeText(this, "Ficha Médica de Emergência em breve!", Toast.LENGTH_SHORT).show()
        }
    }
}