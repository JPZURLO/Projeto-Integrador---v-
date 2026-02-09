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

        // 1. Saudação
        val txtOla = findViewById<TextView>(R.id.txtOla)
        val nomeUsuario = intent.getStringExtra("NOME_USUARIO") ?: "João"
        txtOla.text = "Olá, $nomeUsuario"

        // 2. Mapear os Cards
        val cardSinais = findViewById<MaterialCardView>(R.id.cardSinais)
        val cardAgenda = findViewById<MaterialCardView>(R.id.cardAgenda) // Já existe no seu XML
        val cardAgua = findViewById<MaterialCardView>(R.id.cardAgua)
        val cardEmergencia = findViewById<MaterialCardView>(R.id.cardEmergencia)

        // 3. Configurar Cliques

        // SINAIS VITAIS
        cardSinais.setOnClickListener {
            val intent = Intent(this, SinaisVitaisActivity::class.java)
            startActivity(intent)
        }

        // === AGENDA (AGORA FUNCIONA!) ===
        cardAgenda.setOnClickListener {
            val intent = Intent(this, AgendaActivity::class.java)
            startActivity(intent)
        }

        // Outros (Futuros)
        cardAgua.setOnClickListener {
            Toast.makeText(this, "Bot de Hidratação em breve!", Toast.LENGTH_SHORT).show()
        }

        cardEmergencia.setOnClickListener {
            Toast.makeText(this, "Ficha Médica em breve!", Toast.LENGTH_SHORT).show()
        }
    }
}