package com.example.gerokernel.api
import com.example.gerokernel.models.SinaisModel
import com.example.gerokernel.model.User
import com.example.gerokernel.models.AtualizarPerfilRequest
import com.example.gerokernel.models.ConsultaModel
import com.example.gerokernel.models.FichaMedicaResponse
import com.example.gerokernel.models.HidratacaoRequest
import com.example.gerokernel.models.HidratacaoResponse
import com.example.gerokernel.models.Medicamento
import com.example.gerokernel.models.MedicamentoRequest
import com.example.gerokernel.models.TomarMedicamentoResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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
    // No ApiService.kt
    // Na sua Interface, mude o retorno para Void para aceitar o Callback que você já escreveu
    @POST("consultas")
    fun salvarConsulta(@Body consulta: ConsultaModel): Call<Void>

    @GET("consultas/{id}")
    fun listarConsultas(@Path("id") usuarioId: Int): Call<List<ConsultaModel>>

    @DELETE("consultas/{id}")
    fun deletarConsulta(@Path("id") id: Int): Call<Void>

    // HIDRATAÇÃO
    @POST("hidratacao")
    fun salvarHidratacao(@Body corpo: HidratacaoRequest): Call<Void>

    @GET("hidratacao/{id}")
    fun getHistoricoHidratacao(@Path("id") id: Int): Call<List<HidratacaoResponse>>


    // FICHA MÉDICA
    @GET("ficha/{id}")
    fun getFicha(@Path("id") id: Int): Call<FichaMedicaResponse>

    @GET("ficha/{id}")
    fun getFichaMedica(@Path("id") id: Int): Call<FichaMedicaResponse>

    // Atualize o retorno do 'tomar' para a classe que criamos no Passo 1
    @POST("medicamentos/tomar/{id}")
    fun tomarMedicamento(@Path("id") id: Int): Call<TomarMedicamentoResponse>

    // Atualize o salvar para o cronograma
    @POST("medicamentos")
    fun salvarMedicamento(@Body dados: MedicamentoRequest): Call<Void>

    // Certifique-se de que o nome é getMedicamentos para bater com a Activity
    @GET("medicamentos/{usuario_id}")
    fun getMedicamentos(@Path("usuario_id") usuarioId: Int): Call<List<Medicamento>>

    @PUT("perfil/{id}")
    fun atualizarPerfil(
        @Path("id") id: Int,
        @Body dados: AtualizarPerfilRequest
    ): Call<Void> // Certifique-se de que o retorno é Call<Void>

    @DELETE("medicamentos/{id}")
    fun excluirMedicamento(@Path("id") id: Int): Call<Void>


}


