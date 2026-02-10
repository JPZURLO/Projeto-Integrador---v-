package com.example.gerokernel.api

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Modelos de Resposta (JSON)
data class WeatherResponse(val main: MainData, val name: String)
data class MainData(val temp: Double, val humidity: Int)

interface WeatherApi {
    @GET("data/2.5/weather")
    fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "pt_br",
        @Query("appid") apiKey: String
    ): Call<WeatherResponse>
}

// Cliente Retrofit Específico para o Clima
object WeatherClient {
    private const val BASE_URL = "https://api.openweathermap.org/"

    val instance: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}