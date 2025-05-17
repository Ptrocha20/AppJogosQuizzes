package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultadoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_resultado)

        val resultText: TextView = findViewById(R.id.resultText)

        val score = intent.getIntExtra("SCORE", 0)
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 0)

        resultText.text = "$score/$totalQuestions acertos"

        // Registar o resultado do quiz na base de dados usando o ProgressoManager
        ProgressoManager.finalizarQuiz(this, score, totalQuestions)

        findViewById<Button>(R.id.BackButton).setOnClickListener {
            val intent = Intent(this, JogosActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}