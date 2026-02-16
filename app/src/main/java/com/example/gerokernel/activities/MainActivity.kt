package com.example.gerokernel.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
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

// IMPORTANTE: Se o erro persistir, adicione este import manualmente
import com.example.gerokernel.activities.AgendaActivity

class MainActivity : AppCompatActivity() {

    private val API_KEY = "1847f643fb8e8e81297ab519e2c1132a"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupCards()
        buscarLocalizacaoEClima()
    }

    override fun onResume() {
        super.onResume()
        atualizarSaudacao()
    }

    private fun atualizarSaudacao() {
        val txtOla = findViewById<TextView>(R.id.txtOla)
        val prefs = getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE)
        val nomeSalvo = prefs.getString("NOME_USUARIO", null)
        val nomeIntent = intent.getStringExtra("NOME_USUARIO")

        val nomeFinal = nomeSalvo ?: nomeIntent ?: "Usuário"
        txtOla.text = "Olá, $nomeFinal"
    }

    private fun setupCards() {
        val imgPerfil = findViewById<ImageView>(R.id.imgPerfil)
        imgPerfil?.setOnClickListener {
            val intent = Intent(this@MainActivity, PerfilActivity::class.java)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardSinais).setOnClickListener {
            startActivity(Intent(this@MainActivity, SinaisVitaisActivity::class.java))
        }

        // CORREÇÃO: Uso explícito da classe para evitar erro de inferência
        findViewById<MaterialCardView>(R.id.cardAgenda).setOnClickListener {
            val intent = Intent(this@MainActivity, AgendaActivity::class.java)
            startActivity(intent)
        }

        findViewById<MaterialCardView>(R.id.cardAgua).setOnClickListener {
            startActivity(Intent(this@MainActivity, HidratacaoActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardEmergencia).setOnClickListener {
            startActivity(Intent(this@MainActivity, EmergenciaActivity::class.java))
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun buscarLocalizacaoEClima() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
                }
            }
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                txtTemp.text = "Erro rede"
            }
        })
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) buscarLocalizacaoEClima()
    }
}