package com.example.gerokernel.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gerokernel.R
import com.example.gerokernel.api.RetrofitClient
import com.example.gerokernel.model.User
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CadastroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        // 1. Inicializa os componentes (Foco em acessibilidade)
        val editNome = findViewById<TextInputEditText>(R.id.editNome)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val editCpf = findViewById<TextInputEditText>(R.id.editCpf)
        val editDataNasc = findViewById<TextInputEditText>(R.id.editDataNasc)
        val editSenha = findViewById<TextInputEditText>(R.id.editSenha)
        val checkMostrarSenha = findViewById<CheckBox>(R.id.checkMostrarSenha)
        val btnFinalizar = findViewById<Button>(R.id.btnFinalizar)

        // Novos botões de navegação
        val btnSetaVoltar = findViewById<ImageButton>(R.id.btnSetaVoltar)
        val btnVoltarLogin = findViewById<Button>(R.id.btnVoltarLogin)

        // 2. Navegação de volta para o Login
        btnSetaVoltar.setOnClickListener { finish() }
        btnVoltarLogin.setOnClickListener { finish() }

        // 3. Aplica as Máscaras para facilitar o preenchimento
        aplicaMascara(editCpf, "###.###.###-##")
        aplicaMascara(editDataNasc, "##/##/####")

        // 4. Lógica de Mostrar Senha (Reduz erro do idoso)
        checkMostrarSenha.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editSenha.transformationMethod = null
            } else {
                editSenha.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            editSenha.setSelection(editSenha.text?.length ?: 0)
        }

        // 5. Clique do Botão (Envio para Node.js + MySQL 8)
        btnFinalizar.setOnClickListener {
            val nome = editNome.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val cpf = editCpf.text.toString().trim()
            val dataNasc = editDataNasc.text.toString().trim()
            val senha = editSenha.text.toString().trim()

            // Validação básica profissional
            if (nome.isNotEmpty() && email.isNotEmpty() && senha.isNotEmpty() && cpf.length == 14) {
                Toast.makeText(this, "Salvando seus dados...", Toast.LENGTH_SHORT).show()

                val novoUsuario = User(0, email, senha, cpf, dataNasc, "IDOSO")

                // Chamada via Retrofit
                RetrofitClient.instance.cadastrarUsuario(novoUsuario).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@CadastroActivity, "Cadastro realizado! Agora faça seu login.", Toast.LENGTH_LONG).show()
                            finish() // Retorna para a LoginActivity
                        } else {
                            Log.e("GEROKERNEL", "Erro ${response.code()}: ${response.errorBody()?.string()}")
                            Toast.makeText(this@CadastroActivity, "Erro no servidor. Tente novamente.", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e("ERRO_GEROKERNEL", "Falha: ${t.message}")
                        Toast.makeText(this@CadastroActivity, "Sem conexão com o servidor.", Toast.LENGTH_LONG).show()
                    }
                })
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos corretamente.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função de Máscara Genérica
    private fun aplicaMascara(editText: EditText, mask: String) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private var oldText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str = s.toString().replace(Regex("[.\\-/]"), "")
                var result = ""

                if (isUpdating) {
                    oldText = str
                    isUpdating = false
                    return
                }

                var i = 0
                for (m in mask.toCharArray()) {
                    if (m != '#' && str.length > oldText.length) {
                        result += m
                        continue
                    }
                    try {
                        result += str[i]
                    } catch (e: Exception) {
                        break
                    }
                    i++
                }

                isUpdating = true
                editText.setText(result)
                editText.setSelection(result.length)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}