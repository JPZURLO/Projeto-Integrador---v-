package com.example.gerokernel.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.gerokernel.R
import com.example.gerokernel.api.WeatherClient
import com.example.gerokernel.api.WeatherResponse
import com.google.android.gms.location.LocationServices
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    // Sua chave da API (Cole a sua aqui!)
    private val API_KEY = "1847f643fb8e8e81297ab519e2c1132a"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ... (Seu código de saudação e cards existentes) ...
        val txtOla = findViewById<TextView>(R.id.txtOla)
        val nomeUsuario = intent.getStringExtra("NOME_USUARIO") ?: "Usuário"
        txtOla.text = "Olá, $nomeUsuario"

        // Setup dos Cards Antigos
        setupCards()

        // === NOVO: BUSCAR CLIMA ===
        buscarLocalizacaoEClima()
    }

    private fun setupCards() {
        // 1. Sinais Vitais (Confira se o ID no XML é @+id/cardSinais)
        findViewById<MaterialCardView>(R.id.cardSinais).setOnClickListener {
            val intent = Intent(this, SinaisVitaisActivity::class.java)
            startActivity(intent)
        }

        // 2. Agenda (Confira se o ID no XML é @+id/cardAgenda)
        findViewById<MaterialCardView>(R.id.cardAgenda).setOnClickListener {
            val intent = Intent(this, AgendaActivity::class.java)
            startActivity(intent)
        }

        // 3. Hidratação (Confira se o ID no XML é @+id/cardAgua)
        findViewById<MaterialCardView>(R.id.cardAgua).setOnClickListener {
            val intent = Intent(this, HidratacaoActivity::class.java)
            startActivity(intent)
        }

        // 4. Emergência (Esse a gente ainda vai criar, então só mostra mensagem)
        findViewById<MaterialCardView>(R.id.cardEmergencia).setOnClickListener {
            Toast.makeText(this, "Tela de Emergência em breve!", Toast.LENGTH_SHORT).show()
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun buscarLocalizacaoEClima() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Verifica Permissão
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                atualizarClima(location.latitude, location.longitude)
            } else {
                findViewById<TextView>(R.id.txtTemperatura).text = "--°C"
                findViewById<TextView>(R.id.txtUmidade).text = "Local não encontrado"
            }
        }
    }

    private fun atualizarClima(lat: Double, lon: Double) {
        val txtTemp = findViewById<TextView>(R.id.txtTemperatura)
        val txtUmid = findViewById<TextView>(R.id.txtUmidade)

        WeatherClient.instance.getWeather(lat, lon, apiKey = API_KEY).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val clima = response.body()
                    val temperatura = clima?.main?.temp?.toInt() ?: 0
                    val umidade = clima?.main?.humidity ?: 0
                    val cidade = clima?.name ?: "Local"

                    txtTemp.text = "$temperatura°C em $cidade"
                    txtUmid.text = "Umidade: $umidade%"

                    // Lógica Extra: Avisos de Saúde!
                    if (umidade < 30) txtUmid.text = "Umidade: $umidade% (AR SECO: Beba água!)"
                    if (temperatura > 32) txtTemp.text = "$temperatura°C (CALOR: Cuidado com a pressão)"
                }
            }
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                txtTemp.text = "Erro na rede"
            }
        })
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) buscarLocalizacaoEClima()
    }
}