package com.example.gerokernel.activities // Certifique-se que o package bate com sua pasta!

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.gerokernel.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Vamos criar esse XML já já!

        // Timer de 3 segundos para dar um ar profissional ao PI-V
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Fecha a splash para o idoso não voltar nela ao clicar em "voltar"
        }, 3000)
    }
}