package com.example.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ConquistaAdapter(
    private val todasConquistas: Map<String, String>,
    private val conquistasObtidas: List<String>
) : RecyclerView.Adapter<ConquistaAdapter.ConquistaViewHolder>() {

    private val conquistasList = todasConquistas.keys.toList()


    private val recompensaPorConquista = mapOf(
        "JOGOS_2" to "+20 XP",
        "JOGOS_5" to "+50 XP",
        "JOGOS_10" to "+100 XP",
        "QUIZZES_3" to "+30 XP",
        "QUIZZES_7" to "+70 XP",
        "QUIZZES_15" to "+150 XP",
        "NIVEL_3" to "+50 XP",
        "NIVEL_5" to "+100 XP"
    )

    class ConquistaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titulo: TextView = view.findViewById(R.id.tvTitulo)
        val descricao: TextView = view.findViewById(R.id.tvDescricao)
        val recompensa: TextView = view.findViewById(R.id.tvRecompensa)
        val icone: ImageView = view.findViewById(R.id.imgIcone)
        val cardView: CardView = view.findViewById(R.id.cardConquista)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConquistaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_conquista, parent, false
        )
        return ConquistaViewHolder(view)
    }

    override fun getItemCount(): Int = conquistasList.size

    override fun onBindViewHolder(holder: ConquistaViewHolder, position: Int) {
        val conquistaId = conquistasList[position]
        val conquistaNome = todasConquistas[conquistaId] ?: "Conquista Desconhecida"
        val obtida = conquistasObtidas.contains(conquistaId)

        holder.titulo.text = conquistaNome

        // Adicione descrição para cada conquista
        holder.descricao.text = when (conquistaId) {
            "JOGOS_2" -> "Completa 2 jogos de informática"
            "JOGOS_5" -> "Completa 5 jogos de informática"
            "JOGOS_10" -> "Completa 10 jogos de informática"
            "QUIZZES_3" -> "Completa 3 quizzes de conhecimento"
            "QUIZZES_7" -> "Completa 7 quizzes de conhecimento"
            "QUIZZES_15" -> "Completa 15 quizzes de conhecimento"
            "NIVEL_3" -> "Alcança o nível 3 de experiência"
            "NIVEL_5" -> "Alcança o nível 5 de experiência"
            else -> "Conquista especial"
        }

        // Configurar recompensa
        holder.recompensa.text = recompensaPorConquista[conquistaId] ?: "+0 XP"

        // Configurar ícone apropriado baseado no tipo de conquista
        val iconResource = when {
            conquistaId.startsWith("JOGOS") -> R.drawable.ic_game
            conquistaId.startsWith("QUIZZES") -> R.drawable.ic_quiz
            conquistaId.startsWith("NIVEL") -> R.drawable.ic_trending_up
            else -> R.drawable.ic_achievement_placeholder
        }


        try {
            holder.icone.setImageResource(iconResource)
        } catch (e: Exception) {
            holder.icone.setImageResource(R.drawable.ic_achievement_placeholder)
        }


        if (obtida) {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.colorAchievementCompleted)
            )
            holder.titulo.alpha = 1.0f
            holder.descricao.alpha = 1.0f
            holder.icone.alpha = 1.0f
        } else {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.colorAchievementLocked)
            )
            holder.titulo.alpha = 0.6f
            holder.descricao.alpha = 0.6f
            holder.icone.alpha = 0.6f
        }
    }
}
