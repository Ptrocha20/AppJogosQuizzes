package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ResultadoActivityJogoDefesaRede : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var utilizador: String? = null
    private var xpGanho: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_result_jogodefesa)

        db = FirebaseFirestore.getInstance()

        val finalScore = intent.getIntExtra("FINAL_SCORE", 0)

        findViewById<TextView>(R.id.scoreText).text = "Pontuação Final: $finalScore"

        xpGanho = calcularXP(finalScore)

        findViewById<TextView>(R.id.xpText).text = "XP: $xpGanho"

        utilizador = intent.getStringExtra("EMAIL")

        // Registrar a pontuação do jogo no sistema de progresso e conquistas
        registrarPontuacaoJogo(finalScore)

        findViewById<Button>(R.id.VoltarButton).setOnClickListener {
            val intent = Intent(this, JogosActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun calcularXP(pontuacao: Int): Int {
        return pontuacao * 3
    }

    private fun registrarPontuacaoJogo(pontuacao: Int) {

        ProgressoManager.finalizarJogo(this, pontuacao)

        // Exibir mensagem de XP ganho
        Toast.makeText(this, "Você ganhou $xpGanho XP!", Toast.LENGTH_SHORT).show()
    }
}