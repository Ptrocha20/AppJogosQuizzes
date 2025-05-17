package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RecuperarSenhaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.recuperacao_palavra_passe)

        auth = FirebaseAuth.getInstance()

        val botaoRecuperar = findViewById<Button>(R.id.seguinte)
        val emailInput = findViewById<EditText>(R.id.insere_email)
        val botaoVoltar = findViewById<ImageView>(R.id.voltar)

        // Botão de recuperação de senha
        botaoRecuperar.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, insira um email!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Email de recuperação enviado!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Botão de voltar
        botaoVoltar.setOnClickListener {
            finish() // Fecha a atividade atual e volta para a anterior
        }
    }
}