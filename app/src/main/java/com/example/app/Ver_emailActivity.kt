package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Ver_emailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.ver_o_email)

        val botaoVoltar = findViewById<ImageView>(R.id.voltar)

        findViewById<TextView>(R.id.voltar_login).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        // Botão de voltar
        botaoVoltar.setOnClickListener {
            finish() // Fecha a atividade atual e volta para a anterior
        }
    }
}

