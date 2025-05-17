package com.example.app

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object ProgressoManager {
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

    fun finalizarJogo(context: Context, pontuacao: Int) {
        // Verificar se é o jogo de defesa de rede (pela context class name)
        val isJogoDefesaRede = context.javaClass.simpleName == "ResultadoActivityJogoDefesaRede"

        // Calcular XP baseado no tipo de jogo
        val xpGanho = if (isJogoDefesaRede) {
            // Usar a lógica específica do jogo de defesa de rede
            pontuacao * 3
        } else {
            // Usar a lógica padrão para outros jogos
            when {
                pontuacao >= 90 -> 50  // Excelente performance
                pontuacao >= 70 -> 30  // Boa performance
                pontuacao >= 50 -> 20  // Performance média
                else -> 10             // Performance baixa
            }
        }

        adicionarXP(context, xpGanho, "jogo")
    }

    fun finalizarQuiz(context: Context, respostasCorretas: Int, totalPerguntas: Int) {
        // Calcular XP baseado na proporção de respostas corretas
        val percentagemAcerto = (respostasCorretas.toFloat() / totalPerguntas) * 100
        val xpGanho = when {
            percentagemAcerto >= 90 -> 40  // Excelente performance
            percentagemAcerto >= 70 -> 25  // Boa performance
            percentagemAcerto >= 50 -> 15  // Performance média
            else -> 5                       // Performance baixa
        }

        adicionarXP(context, xpGanho, "quiz")
    }

    private fun adicionarXP(context: Context, xpGanho: Int, tipoAtividade: String) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: ""

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
                            // Mostrar notificações para conquistas desbloqueadas
                            val conquistasNovas = novasConquistas.filter { !conquistasObtidas.contains(it) }.toSet()
                            mostrarConquistasDesbloqueadas(context, conquistasNovas)

                            // Verificar se subiu de nível e mostrar notificação
                            if (novoNivel > nivelAtual) {
                                mostrarNotificacaoNovoNivel(context, novoNivel)
                            }
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

    private fun mostrarConquistasDesbloqueadas(context: Context, novasConquistas: Set<String>) {
        for (conquistaId in novasConquistas) {
            val nomeConquista = conquistas[conquistaId] ?: "Conquista Desbloqueada"
            Toast.makeText(
                context,
                "🏆 Nova Conquista: $nomeConquista",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun mostrarNotificacaoNovoNivel(context: Context, novoNivel: Int) {
        val titulo = tituloPorNivel[novoNivel] ?: ""
        Toast.makeText(
            context,
            "🎉 Parabéns! Subiu para o Nível $novoNivel: $titulo",
            Toast.LENGTH_LONG
        ).show()
    }
}