package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class JogosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.jogosquizzes)

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

        val playButton1 = findViewById<Button>(R.id.playButton1)

        playButton1.setOnClickListener {

            val intent = Intent(this, RoletaQuizActivity::class.java)
            startActivity(intent)
        }

        val playButton2: Button = findViewById(R.id.playButton2)

        playButton2.setOnClickListener {

            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        val playButton3: Button = findViewById(R.id.playButton3)

        playButton3.setOnClickListener {

            val intent = Intent(this, DefensorDigitalActivity::class.java)
            startActivity(intent)
        }

    }
}
