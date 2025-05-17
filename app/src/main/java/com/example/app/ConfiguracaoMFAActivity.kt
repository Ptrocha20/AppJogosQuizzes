package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.security.SecureRandom

class ConfiguracaoMFAActivity : AppCompatActivity() {

    private lateinit var editTextTelefone: EditText
    private lateinit var buttonAtivarMFA: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.autenticacao_multifator)

        editTextTelefone = findViewById(R.id.editTextTelefone)
        buttonAtivarMFA = findViewById(R.id.buttonAtivarMFA)

        buttonAtivarMFA.setOnClickListener {
            val numeroInserido = editTextTelefone.text.toString().trim()

            // Validação mais robusta do número de telemóvel
            if (validarNumeroTelemovel(numeroInserido)) {
                // Gera código de verificação
                val codigoVerificacao = gerarCodigoVerificacao()

                // Navega para tela de verificação
                val intent = Intent(this, VerificacaoMFAActivity::class.java).apply {
                    putExtra("NUMERO_TELEMOVEL", numeroInserido)
                }
                startActivity(intent)
            } else {
                editTextTelefone.error = "Número de telemóvel inválido"
                Toast.makeText(this, "Por favor, introduza um número de telemóvel válido.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun gerarCodigoVerificacao(): String {
        // Gera código de 6 dígitos
        val codigoVerificacao = String.format("%06d", SecureRandom().nextInt(1000000))

        // Salva código em preferências
        val prefs = getSharedPreferences("MFA_PREFS", MODE_PRIVATE)
        prefs.edit()
            .putString("CODIGO_VERIFICACAO", codigoVerificacao)
            .apply()

        // Log do código (APENAS PARA DESENVOLVIMENTO)
        println("Código de Verificação: $codigoVerificacao")

        return codigoVerificacao
    }

    private fun validarNumeroTelemovel(numero: String): Boolean {
        // Validação para números portugueses (9 seguido de 1, 2, 3 ou 6 e mais 7 dígitos)
        val regexTelemovel = "^(9[1236][0-9]{7})$".toRegex()
        return numero.matches(regexTelemovel)
    }
}