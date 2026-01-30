package com.example.gerokernel.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gerokernel.R
import com.example.gerokernel.api.RetrofitClient
import com.exemplo.gerokernel.models.User
import com.google.android.material.textfield.TextInputEditText

class CadastroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        // 1. Inicializa os componentes do layout profissional
        val editNome = findViewById<TextInputEditText>(R.id.editNome)
        val editEmail = findViewById<TextInputEditText>(R.id.editEmail)
        val editCpf = findViewById<TextInputEditText>(R.id.editCpf)
        val editDataNasc = findViewById<TextInputEditText>(R.id.editDataNasc)
        val editSenha = findViewById<TextInputEditText>(R.id.editSenha)
        val checkMostrarSenha = findViewById<CheckBox>(R.id.checkMostrarSenha)
        val btnFinalizar = findViewById<Button>(R.id.btnFinalizar)

        // 2. Aplica as Máscaras (Usabilidade para o idoso)
        aplicaMascara(editCpf, "###.###.###-##")
        aplicaMascara(editDataNasc, "##/##/####")

        // 3. Lógica de Mostrar Senha
        checkMostrarSenha.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editSenha.transformationMethod = null
            } else {
                editSenha.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            editSenha.setSelection(editSenha.text?.length ?: 0)
        }

        // 4. Clique do Botão (A ponte para o MySQL 8)
        btnFinalizar.setOnClickListener {
            val nome = editNome.text.toString()
            val email = editEmail.text.toString()
            val cpf = editCpf.text.toString()
            val dataNasc = editDataNasc.text.toString()
            val senha = editSenha.text.toString()

            if (nome.isNotEmpty() && email.isNotEmpty() && senha.isNotEmpty()) {
                Toast.makeText(this, "Enviando para o servidor...", Toast.LENGTH_SHORT).show()

                // Criamos o objeto com os dados (Certifique-se de ter a classe User criada)
                val novoUsuario = User(nome, email, senha, cpf, dataNasc, "IDOSO")

                // DISPARAMOS A CHAMADA REAL VIA RETROFIT
                RetrofitClient.instance.cadastrarUsuario(novoUsuario).enqueue(object : retrofit2.Callback<Void> {
                    override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@CadastroActivity, "Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show()
                            finish() // Fecha a tela de cadastro
                        } else {
                            Toast.makeText(this@CadastroActivity, "Erro no servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                        // Se cair aqui, olhe o Logcat! Pode ser o IP 10.0.2.2
                        android.util.Log.e("ERRO_GEROKERNEL", "Falha de rede: ${t.message}")
                        Toast.makeText(this@CadastroActivity, "Erro de conexão: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })

            } else {
                Toast.makeText(this, "Preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função de Máscara Genérica (CPF e Data)
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