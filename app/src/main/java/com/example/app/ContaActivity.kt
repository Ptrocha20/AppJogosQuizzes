package com.example.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ContaActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var tvNomeUtilizador: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvNivel: TextView
    private lateinit var tvNivelTitulo: TextView
    private lateinit var progressNivel: ProgressBar
    private lateinit var auth: FirebaseAuth


    private val xpPorNivel = mapOf(
        1 to 100,  // Para atingir o nível 2, precisa de 100 XP
        2 to 250,  // Para atingir o nível 3, precisa de 250 XP
        3 to 500,  // Para atingir o nível 4, precisa de 500 XP
        4 to 1000, // Para atingir o nível 5, precisa de 1000 XP
        5 to 2000  // Para atingir o nível 6, precisa de 2000 XP
    )


    private val tituloPorNivel = mapOf(
        1 to "Novato",
        2 to "Aprendiz",
        3 to "Intermédio",
        4 to "Avançado",
        5 to "Especialista",
        6 to "Mestre"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.conta)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        tvNomeUtilizador = findViewById(R.id.tvNomeUtilizador)
        tvEmail = findViewById(R.id.tvEmail)
        tvNivel = findViewById(R.id.tvNivel)
        progressNivel = findViewById(R.id.progressNivel)


        try {
            tvNivelTitulo = findViewById(R.id.tvNivelTitulo)
        } catch (e: Exception) {
            // Se não encontrar, talvez seja porque estamos usando outro ID
            Log.e("ContaActivity", "Erro ao encontrar tvNivelTitulo: ${e.message}")
        }

        // Mostrar email do utilizador autenticado
        val userEmail = auth.currentUser?.email
        tvEmail.text = userEmail ?: "Email não disponível"

        if (userEmail != null) {
            obterDadosUtilizador(userEmail)
        } else {
            tvNomeUtilizador.text = "Erro ao carregar nome"
            Log.e("Firestore", "Erro: Nenhum utilizador autenticado.")
        }

        val cardEditarPerfil = findViewById<CardView>(R.id.cardEditarPerfil)

        cardEditarPerfil.setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java)
            startActivity(intent)
        }

        val cardProgresso = findViewById<CardView>(R.id.cardProgresso)

        cardProgresso.setOnClickListener {
            val intent = Intent(this, ProgressoConquistasActivity::class.java)
            startActivity(intent)
        }

        val cardConquistas = findViewById<CardView>(R.id.cardConquistas)

        cardConquistas.setOnClickListener {
            val intent = Intent(this, TodasConquistasActivity::class.java)
            startActivity(intent)
        }


        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)


        bottomNavigationView.selectedItemId = R.id.nav_profile

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
                    // Já estamos nesta atividade
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, DefinicoesActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun obterDadosUtilizador(email: String) {
        Log.d("Firestore", "A tentar buscar utilizador com email: $email")

        db.collection("Utilizadores")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val userId = document.id
                        val nome = document.getString("nome") ?: "Nome não encontrado"
                        val nivel = document.getLong("nivel")?.toInt() ?: 1
                        val xp = document.getLong("xp")?.toInt() ?: 0

                        tvNomeUtilizador.text = nome
                        atualizarBarraProgresso(nivel, xp)

                        Log.d("Firestore", "Nome encontrado: $nome, Nível: $nivel, XP: $xp")
                        break // Parar após o primeiro resultado
                    }
                } else {
                    tvNomeUtilizador.text = "Utilizador não encontrado"
                    Log.e("Firestore", "Nenhum documento encontrado para este email.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Erro ao buscar utilizador", exception)
                tvNomeUtilizador.text = "Erro ao carregar nome"
            }
    }

    private fun atualizarBarraProgresso(nivel: Int, xp: Int) {

        val nivelTexto = "Nível $nivel - ${tituloPorNivel[nivel] ?: "Desconhecido"}"


        if (::tvNivelTitulo.isInitialized) {
            tvNivel.text = nivel.toString()
            tvNivelTitulo.text = tituloPorNivel[nivel] ?: "Desconhecido"
        } else {

            tvNivel.text = nivelTexto
        }

        // Calcular XP para o próximo nível
        val xpProximoNivel = xpPorNivel[nivel] ?: 2000

        // Configurar a barra de progresso
        progressNivel.max = xpProximoNivel
        progressNivel.progress = xp

        Log.d("ContaActivity", "Barra de progresso atualizada: $xp/$xpProximoNivel")
    }
}