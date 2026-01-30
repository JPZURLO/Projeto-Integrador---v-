package com.exemplo.gerokernel.api

import com.exemplo.gerokernel.models.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("cadastro")
    fun cadastrarUsuario(@Body user: User): Call<Void>
}