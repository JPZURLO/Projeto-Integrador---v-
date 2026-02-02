package com.exemplo.gerokernel.api

import com.exemplo.gerokernel.models.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("cadastro")
    fun cadastrarUsuario(@Body user: User): Call<Void>

    @POST("login")
    fun fazerLogin(@Body request: LoginRequest): Call<User>
    data class LoginRequest(val email: String, val senha: String)

    // ... suas outras rotas ...

    // Rota para PEDIR o e-mail (Tela "Esqueceu a Senha")
    @POST("recuperar-senha")
    fun solicitarRecuperacao(@Body body: Map<String, String>): Call<Void>

    // Rota para SALVAR a nova senha (Tela "Redefinir Senha")
    @POST("redefinir-senha")
    fun salvarNovaSenha(@Body body: Map<String, String>): Call<Void>
}

