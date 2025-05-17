package com.example.app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class ResultadoDefensorDigitalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_resultado_defensor_digital)

        // Obtém a pontuação da Intent
        val pontuacao = intent.getIntExtra("pontuacao", 0)

        // Configura as views
        findViewById<TextView>(R.id.tvPontuacaoFinal).text = "Pontuação Final: $pontuacao"

        // Calcula as estatísticas baseadas na pontuação
        val nivel = when {
            pontuacao >= 200 -> "Especialista em Segurança Digital"
            pontuacao >= 150 -> "Avançado"
            pontuacao >= 100 -> "Intermediário"
            else -> "Iniciante"
        }

        findViewById<TextView>(R.id.tvNivelObtido).text = "Nível de Segurança: $nivel"

        // Botões
        findViewById<Button>(R.id.btnJogarNovamente).setOnClickListener {
            // Regista o progresso antes de iniciar novo jogo
            ProgressoManager.finalizarJogo(this, pontuacao)
            // Volta para a tela anterior
            finish()
        }

        findViewById<Button>(R.id.btnVoltar).setOnClickListener {
            // Regista o progresso antes de sair
            ProgressoManager.finalizarJogo(this, pontuacao)
            // Volta para o menu principal
            finish()
        }
    }
}