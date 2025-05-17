package com.example.app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TodasConquistasActivity : AppCompatActivity() {

    private lateinit var rvTodasConquistas: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var tvConquistasDesbloqueadas: TextView
    private lateinit var tvTotalConquistas: TextView
    private lateinit var tvPercentagemCompletada: TextView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    // Definição das conquistas
    private val conquistas = mapOf(
        "JOGOS_2" to "Iniciante de Informática",
        "JOGOS_5" to "Entusiasta de Informática",
        "JOGOS_10" to "Especialista de Informática",
        "QUIZZES_3" to "Curioso",
        "QUIZZES_7" to "Estudante Dedicado",
        "QUIZZES_15" to "Mestre do Conhecimento",
        "NIVEL_3" to "Em Ascensão",
        "NIVEL_5" to "Veterano"
    )

    // Conquistar filtradas por categoria
    private val conquistasJogos = conquistas.filterKeys { it.startsWith("JOGOS_") }
    private val conquistasQuizzes = conquistas.filterKeys { it.startsWith("QUIZZES_") }
    private val conquistasNiveis = conquistas.filterKeys { it.startsWith("NIVEL_") }

    private var conquistasObtidas = listOf<String>()
    private var conquistasMostradas = conquistas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.conquistas)

        // Inicializar views
        rvTodasConquistas = findViewById(R.id.rvTodasConquistas)
        tabLayout = findViewById(R.id.tabLayout)
        tvConquistasDesbloqueadas = findViewById(R.id.tvConquistasDesbloqueadas)
        tvTotalConquistas = findViewById(R.id.tvTotalConquistas)
        tvPercentagemCompletada = findViewById(R.id.tvPercentagemCompletada)

        val btnBack = findViewById<android.widget.ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Voltar para a atividade anterior
        }

        // Configurar RecyclerView
        rvTodasConquistas.layoutManager = LinearLayoutManager(this)

        // Carregar dados do utilizador
        carregarDadosConquistas()

        // Configurar TabLayout
        configurarTabs()
    }

    private fun carregarDadosConquistas() {
        if (userId.isEmpty()) {
            atualizarUI(emptyList())
            return
        }

        db.collection("Utilizadores").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    conquistasObtidas = document.get("conquistasObtidas") as? List<String> ?: listOf()
                    atualizarUI(conquistasObtidas)
                } else {
                    atualizarUI(emptyList())
                }
            }
            .addOnFailureListener { e ->
                Log.e("TodasConquistasActivity", "Erro ao carregar conquistas", e)
                android.widget.Toast.makeText(
                    this,
                    "Erro ao carregar dados: ${e.message}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                atualizarUI(emptyList())
            }
    }

    private fun atualizarUI(conquistasObtidas: List<String>) {
        // Atualizar estatísticas
        val totalConquistas = conquistas.size
        val desbloqueadas = conquistasObtidas.size
        val percentagem = if (totalConquistas > 0) {
            (desbloqueadas.toFloat() / totalConquistas) * 100
        } else {
            0f
        }

        tvConquistasDesbloqueadas.text = desbloqueadas.toString()
        tvTotalConquistas.text = totalConquistas.toString()
        tvPercentagemCompletada.text = String.format("%.0f%%", percentagem)

        // Atualizar RecyclerView
        atualizarListaConquistas(conquistasMostradas)
    }

    private fun atualizarListaConquistas(conquistas: Map<String, String>) {
        val adapter = ConquistaAdapter(conquistas, conquistasObtidas)
        rvTodasConquistas.adapter = adapter
    }

    private fun configurarTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> { // Todas
                        conquistasMostradas = conquistas
                        atualizarListaConquistas(conquistasMostradas)
                    }
                    1 -> { // Jogos
                        conquistasMostradas = conquistasJogos
                        atualizarListaConquistas(conquistasMostradas)
                    }
                    2 -> { // Quizzes
                        conquistasMostradas = conquistasQuizzes
                        atualizarListaConquistas(conquistasMostradas)
                    }
                    3 -> { // Níveis
                        conquistasMostradas = conquistasNiveis
                        atualizarListaConquistas(conquistasMostradas)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}