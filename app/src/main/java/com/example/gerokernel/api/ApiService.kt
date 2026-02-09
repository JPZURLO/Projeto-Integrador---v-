package com.example.gerokernel.api
import com.example.gerokernel.models.SinaisModel
import com.example.gerokernel.model.User
import com.example.gerokernel.models.ConsultaModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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

    // Rota 1: Salvar Medição
    @POST("sinais")
    fun salvarSinais(@Body sinais: SinaisModel): Call<SinaisModel>

    // Rota 2: Buscar Histórico (Pega lista de sinais do usuário)
    @GET("sinais/{usuarioId}")
    fun listarSinais(@Path("usuarioId") usuarioId: Int): Call<List<SinaisModel>>

    // === AGENDA ===
    @POST("consultas")
    fun salvarConsulta(@Body consulta: ConsultaModel): Call<ConsultaModel>

    @GET("consultas/{id}")
    fun listarConsultas(@Path("id") usuarioId: Int): Call<List<ConsultaModel>>

    @DELETE("consultas/{id}")
    fun deletarConsulta(@Path("id") idConsulta: Int): Call<Void>

}


