package com.example.app

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import java.security.SecureRandom

class VerificacaoMFAActivity : AppCompatActivity() {

    private lateinit var editTextCodigoVerificacao: EditText
    private lateinit var buttonVerificar: Button
    private lateinit var textViewReenviar: TextView
    private lateinit var textViewContador: TextView

    private var contagemRegressiva: CountDownTimer? = null
    private var numeroTelemovel: String = ""
    private var codigoVerificacao: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.verificar_mfa)

        numeroTelemovel = intent.getStringExtra("NUMERO_TELEMOVEL") ?: ""

        editTextCodigoVerificacao = findViewById(R.id.editTextVerificationCode)
        buttonVerificar = findViewById(R.id.buttonVerificar)
        textViewReenviar = findViewById(R.id.textViewReenviar)
        textViewContador = findViewById(R.id.textViewContador)

        // Gera código de verificação inicial
        gerarCodigoVerificacao()

        // Inicia contagem regressiva
        iniciarContagemRegressiva()

        buttonVerificar.setOnClickListener {
            verificarCodigoENavegar()
        }

        textViewReenviar.setOnClickListener {
            reenviarCodigo()
        }
    }

    private fun gerarCodigoVerificacao() {
        // Gera código de 6 dígitos
        codigoVerificacao = String.format("%06d", SecureRandom().nextInt(1000000))

        // Salva código em preferências (ou poderia enviar via SMS em produção)
        val prefs = getSharedPreferences("MFA_PREFS", MODE_PRIVATE)
        prefs.edit {
            putString("CODIGO_VERIFICACAO", codigoVerificacao)
        }

        // Log do código (APENAS PARA DESENVOLVIMENTO)
        println("Código de Verificação: $codigoVerificacao")
    }

    private fun verificarCodigoENavegar() {
        val codigoInserido = editTextCodigoVerificacao.text.toString().trim()

        if (codigoInserido.isEmpty()) {
            editTextCodigoVerificacao.error = "Código não pode estar vazio"
            return
        }

        // Verifica o código salvo nas preferências
        val prefs = getSharedPreferences("MFA_PREFS", MODE_PRIVATE)
        val codigoSalvo = prefs.getString("CODIGO_VERIFICACAO", "")

        if (codigoInserido == codigoSalvo) {
            // Código correto - ativa MFA
            val prefsApp = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
            prefsApp.edit {
                putBoolean("MFA_ATIVADO", true)
                putString("TELEFONE_MFA", numeroTelemovel)
            }

            val intent = Intent(this, PaginaInicialActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)

            Toast.makeText(this, "Autenticação de dois fatores ativada com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Código de verificação incorreto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun iniciarContagemRegressiva() {
        contagemRegressiva = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val segundosRestantes = millisUntilFinished / 1000
                textViewContador.text = segundosRestantes.toString()
                textViewReenviar.isEnabled = false
            }

            override fun onFinish() {
                textViewContador.text = "0"
                textViewReenviar.isEnabled = true
            }
        }.start()
    }

    private fun reenviarCodigo() {
        // Gera novo código
        gerarCodigoVerificacao()

        // Reinicia o contador
        contagemRegressiva?.cancel()
        iniciarContagemRegressiva()

        Toast.makeText(this, "Novo código gerado", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        contagemRegressiva?.cancel()
    }
}