package com.example.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class DefinicoesActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.opcoes)


        BottomNavigation(this).setupBottomNavigation()


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, PaginaInicialActivity::class.java))
                    true
                }
                R.id.nav_quizzes -> {
                    startActivity(Intent(this, JogosActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ContaActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, DefinicoesActivity::class.java))
                    true
                }
                else -> false
            }
        }

        findViewById<LinearLayout>(R.id.sair_container).setOnClickListener {
            terminarSessao()
        }

        val cardIdioma = findViewById<CardView>(R.id.idioma_card)

        cardIdioma.setOnClickListener {
            val intent = Intent(this, IdiomaActivity::class.java)
            startActivity(intent)
        }

        val cardSobre = findViewById<CardView>(R.id.sobre_card)

        cardSobre.setOnClickListener {
            val intent = Intent(this, SobreActivity::class.java)
            startActivity(intent)
        }
    }


    private fun terminarSessao() {
        FirebaseAuth.getInstance().signOut()

        // Voltar para o ecrã de login
        val intent = Intent(this, EcraDeEntradaActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


}
