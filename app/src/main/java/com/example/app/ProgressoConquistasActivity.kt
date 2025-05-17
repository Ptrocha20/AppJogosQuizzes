package com.example.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.PorterDuff
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.ImageView
import android.util.Log

class ProgressoConquistasActivity : AppCompatActivity() {

    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvNivel: TextView
    private lateinit var tvNivelTitulo: TextView
    private lateinit var tvXP: TextView
    private lateinit var progressXP: ProgressBar
    private lateinit var tvQuizzesCompletados: TextView
    private lateinit var tvJogosCompletados: TextView
    private lateinit var tvConquistasObtidas: TextView
    private lateinit var rvConquistas: RecyclerView
    private lateinit var btnVerTodasConquistas: TextView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    // Definição dos níveis e XP necessário
    private val xpPorNivel = mapOf(
        1 to 100,  // Para atingir o nível 2, precisa de 100 XP
        2 to 250,  // Para atingir o nível 3, precisa de 250 XP
        3 to 500,  // Para atingir o nível 4, precisa de 500 XP
        4 to 1000, // Para atingir o nível 5, precisa de 1000 XP
        5 to 2000  // Para atingir o nível 6, precisa de 2000 XP
    )

    // Definição dos títulos por nível
    private val tituloPorNivel = mapOf(
        1 to "Novato",
        2 to "Aprendiz",
        3 to "Intermédio",
        4 to "Avançado",
        5 to "Especialista",
        6 to "Mestre"
    )

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.nivel_progresso)


        // Inicializar views
        tvUsername = findViewById(R.id.tvUsername)
        tvEmail = findViewById(R.id.tvEmail)
        tvNivel = findViewById(R.id.tvNivel)
        tvNivelTitulo = findViewById(R.id.tvNivelTitulo)
        tvXP = findViewById(R.id.tvXP)
        progressXP = findViewById(R.id.progressXP)
        tvQuizzesCompletados = findViewById(R.id.tvQuizzesCompletados)
        tvJogosCompletados = findViewById(R.id.tvJogosCompletados)
        tvConquistasObtidas = findViewById(R.id.tvConquistasObtidas)
        btnVerTodasConquistas = findViewById(R.id.tvVerTodas)

        rvConquistas = findViewById(R.id.rvConquistas)
        // Configurar o RecyclerView com um LinearLayoutManager
        rvConquistas.layoutManager = LinearLayoutManager(this)


        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener {
            onBackPressed() // ou finish() dependendo da sua necessidade
        }

        btnVerTodasConquistas.setOnClickListener {
            val intent = Intent(this, TodasConquistasActivity::class.java)
            startActivity(intent)
        }

        // Carregar dados do utilizador
        carregarDadosUtilizador()

        // Verificar se existe extras de jogos ou quizzes
        processarExtras()
    }

    private fun carregarDadosUtilizador() {
        if (userId.isNotEmpty()) {
            db.collection("Utilizadores").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nome = document.getString("nome") ?: ""
                        val email = document.getString("email") ?: ""
                        val nivel = document.getLong("nivel")?.toInt() ?: 1
                        val xp = document.getLong("xp")?.toInt() ?: 0
                        val quizzesConcluidos = document.getLong("quizzesConcluidos")?.toInt() ?: 0
                        val jogosConcluidos = document.getLong("jogosConcluidos")?.toInt() ?: 0
                        val conquistasObtidas = document.get("conquistasObtidas") as? List<String> ?: listOf()

                        atualizarUI(nome, email, nivel, xp, quizzesConcluidos, jogosConcluidos, conquistasObtidas)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ProgressoActivity", "Erro ao carregar dados do utilizador", e)
                    // Exibir mensagem de erro ao usuário
                    android.widget.Toast.makeText(
                        this,
                        "Erro ao carregar dados: ${e.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun processarExtras() {
        val extras = intent.extras
        if (extras != null) {
            val tipoAtividade = extras.getString("TIPO_ATIVIDADE", "")

            when (tipoAtividade) {
                "jogo" -> {
                    val pontuacao = extras.getInt("PONTUACAO_JOGO", 0)
                    if (pontuacao > 0) {
                        finalizarJogo(pontuacao)
                    }
                }
                "quiz" -> {
                    val respostasCorretas = extras.getInt("RESPOSTAS_CORRETAS", 0)
                    val totalPerguntas = extras.getInt("TOTAL_PERGUNTAS", 0)
                    if (respostasCorretas > 0 && totalPerguntas > 0) {
                        finalizarQuiz(respostasCorretas, totalPerguntas)
                    }
                }
            }
        }
    }

    private fun atualizarUI(nome: String, email: String, nivel: Int, xp: Int, quizzes: Int, jogos: Int, conquistasObtidas: List<String>) {
        tvUsername.text = nome
        tvEmail.text = email
        tvNivel.text = nivel.toString()
        tvNivelTitulo.text = "(${tituloPorNivel[nivel] ?: ""})"

        // Calcular XP para o próximo nível
        val xpAtual = xp
        val xpProximoNivel = xpPorNivel[nivel] ?: 0
        tvXP.text = "$xpAtual/$xpProximoNivel XP"

        // Configurar barra de progresso
        progressXP.max = xpProximoNivel
        progressXP.progress = xpAtual

        // Estatísticas
        tvQuizzesCompletados.text = quizzes.toString()
        tvJogosCompletados.text = jogos.toString()
        tvConquistasObtidas.text = conquistasObtidas.size.toString()

        // Configurar RecyclerView de conquistas
        val adapter = ConquistaAdapter(conquistas, conquistasObtidas)
        // Verificar se o LayoutManager já está configurado para evitar erros
        if (rvConquistas.layoutManager == null) {
            rvConquistas.layoutManager = LinearLayoutManager(this)
        }
        rvConquistas.adapter = adapter

        // Log para depuração
        Log.d("ProgressoActivity", "Atualizando UI com ${conquistasObtidas.size} conquistas")
    }

    /**
     * Método para adicionar XP após completar um jogo
     * @param xpGanho Quantidade de XP ganho no jogo
     * @param tipoAtividade Tipo de atividade ("jogo" ou "quiz")
     */
    fun adicionarXP(xpGanho: Int, tipoAtividade: String) {
        if (userId.isEmpty()) return

        db.collection("Utilizadores").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nivelAtual = document.getLong("nivel")?.toInt() ?: 1
                    val xpAtual = document.getLong("xp")?.toInt() ?: 0
                    val quizzesConcluidos = document.getLong("quizzesConcluidos")?.toInt() ?: 0
                    val jogosConcluidos = document.getLong("jogosConcluidos")?.toInt() ?: 0
                    val conquistasObtidas = document.get("conquistasObtidas") as? List<String> ?: listOf()

                    // Calcular novo XP
                    var novoXP = xpAtual + xpGanho
                    var novoNivel = nivelAtual

                    // Verificar se subiu de nível
                    while (novoNivel < 6 && novoXP >= (xpPorNivel[novoNivel] ?: Int.MAX_VALUE)) {
                        novoXP -= (xpPorNivel[novoNivel] ?: 0)
                        novoNivel++
                    }

                    // Atualizar contadores de atividades
                    val novosQuizzes = if (tipoAtividade == "quiz") quizzesConcluidos + 1 else quizzesConcluidos
                    val novosJogos = if (tipoAtividade == "jogo") jogosConcluidos + 1 else jogosConcluidos

                    // Verificar novas conquistas
                    val novasConquistas = ArrayList(conquistasObtidas)
                    verificarConquistas(novoNivel, novosQuizzes, novosJogos, novasConquistas)

                    // Atualizar dados no Firestore
                    val dadosAtualizados = hashMapOf(
                        "nivel" to novoNivel,
                        "xp" to novoXP,
                        "quizzesConcluidos" to novosQuizzes,
                        "jogosConcluidos" to novosJogos,
                        "conquistasObtidas" to novasConquistas
                    )

                    db.collection("Utilizadores").document(userId)
                        .set(dadosAtualizados, SetOptions.merge())
                        .addOnSuccessListener {
                            // Recarregar UI com novos dados
                            val nome = document.getString("nome") ?: ""
                            val email = document.getString("email") ?: ""
                            atualizarUI(nome, email, novoNivel, novoXP, novosQuizzes, novosJogos, novasConquistas)

                            // Mostrar notificações para conquistas desbloqueadas
                            val conquistasNovas = novasConquistas.filter { !conquistasObtidas.contains(it) }.toSet()
                            mostrarConquistasDesbloqueadas(conquistasNovas)

                            // Verificar se subiu de nível e mostrar notificação
                            if (novoNivel > nivelAtual) {
                                mostrarNotificacaoNovoNivel(novoNivel)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("ProgressoActivity", "Erro ao atualizar dados", e)
                            android.widget.Toast.makeText(
                                this,
                                "Erro ao atualizar dados: ${e.message}",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
    }

    private fun verificarConquistas(nivel: Int, quizzes: Int, jogos: Int, conquistasAtuais: MutableList<String>) {
        // Verificar conquistas baseadas em jogos
        if (jogos >= 2 && !conquistasAtuais.contains("JOGOS_2")) {
            conquistasAtuais.add("JOGOS_2")
        }
        if (jogos >= 5 && !conquistasAtuais.contains("JOGOS_5")) {
            conquistasAtuais.add("JOGOS_5")
        }
        if (jogos >= 10 && !conquistasAtuais.contains("JOGOS_10")) {
            conquistasAtuais.add("JOGOS_10")
        }

        // Verificar conquistas baseadas em quizzes
        if (quizzes >= 3 && !conquistasAtuais.contains("QUIZZES_3")) {
            conquistasAtuais.add("QUIZZES_3")
        }
        if (quizzes >= 7 && !conquistasAtuais.contains("QUIZZES_7")) {
            conquistasAtuais.add("QUIZZES_7")
        }
        if (quizzes >= 15 && !conquistasAtuais.contains("QUIZZES_15")) {
            conquistasAtuais.add("QUIZZES_15")
        }

        // Verificar conquistas baseadas em nível
        if (nivel >= 3 && !conquistasAtuais.contains("NIVEL_3")) {
            conquistasAtuais.add("NIVEL_3")
        }
        if (nivel >= 5 && !conquistasAtuais.contains("NIVEL_5")) {
            conquistasAtuais.add("NIVEL_5")
        }
    }

    private fun mostrarConquistasDesbloqueadas(novasConquistas: Set<String>) {
        for (conquistaId in novasConquistas) {
            val nomeConquista = conquistas[conquistaId] ?: "Conquista Desbloqueada"
            // Aqui você pode mostrar um Toast, Dialog ou notificação
            // Por exemplo:
            android.widget.Toast.makeText(
                this,
                "🏆 Nova Conquista: $nomeConquista",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun mostrarNotificacaoNovoNivel(novoNivel: Int) {
        val titulo = tituloPorNivel[novoNivel] ?: ""
        android.widget.Toast.makeText(
            this,
            "🎉 Parabéns! Subiu para o Nível $novoNivel: $titulo",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }

    // Exemplo de como chamar o método quando um jogo é concluído
    fun finalizarJogo(pontuacao: Int) {
        // Calcular XP baseado na pontuação (exemplo)
        val xpGanho = when {
            pontuacao >= 90 -> 50  // Excelente performance
            pontuacao >= 70 -> 30  // Boa performance
            pontuacao >= 50 -> 20  // Performance média
            else -> 10             // Performance baixa
        }

        adicionarXP(xpGanho, "jogo")
    }

    // Exemplo de como chamar o método quando um quiz é concluído
    fun finalizarQuiz(respostasCorretas: Int, totalPerguntas: Int) {
        // Calcular XP baseado na proporção de respostas corretas
        val percentagemAcerto = (respostasCorretas.toFloat() / totalPerguntas) * 100
        val xpGanho = when {
            percentagemAcerto >= 90 -> 40  // Excelente performance
            percentagemAcerto >= 70 -> 25  // Boa performance
            percentagemAcerto >= 50 -> 15  // Performance média
            else -> 5                       // Performance baixa
        }

        adicionarXP(xpGanho, "quiz")
    }
}