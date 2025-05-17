package com.example.app

import android.content.Context
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Classe auxiliar para gerenciar a exibição do progresso do usuário em diferentes activities
 */
class ProgressoUIHelper(private val context: Context) {

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

    /**
     * Atualiza a UI de progresso com os dados do usuário
     */
    fun atualizarBarraProgresso(
        tvNivel: TextView,
        tvNivelTitulo: TextView? = null,
        tvXP: TextView,
        progressXP: ProgressBar,
        nivel: Int,
        xp: Int
    ) {
        // Atualizar informação de nível
        tvNivel.text = nivel.toString()

        // Atualizar título do nível, se a TextView existir
        tvNivelTitulo?.text = "(${tituloPorNivel[nivel] ?: ""})"

        // Calcular XP para o próximo nível
        val xpAtual = xp
        val xpProximoNivel = xpPorNivel[nivel] ?: 0
        tvXP.text = "$xpAtual/$xpProximoNivel XP"

        // Configurar barra de progresso
        progressXP.max = xpProximoNivel
        progressXP.progress = xpAtual
    }

    /**
     * Obtém os dados de nível e XP de um documento do Firestore
     */
    fun obterDadosDeProgresso(document: DocumentSnapshot): Pair<Int, Int> {
        val nivel = document.getLong("nivel")?.toInt() ?: 1
        val xp = document.getLong("xp")?.toInt() ?: 0
        return Pair(nivel, xp)
    }

    /**
     * Retorna o título correspondente ao nível
     */
    fun obterTituloDoNivel(nivel: Int): String {
        return tituloPorNivel[nivel] ?: "Desconhecido"
    }
}