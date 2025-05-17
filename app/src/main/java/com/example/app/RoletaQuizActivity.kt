package com.example.app

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import kotlin.random.Random

class RoletaQuizActivity : AppCompatActivity() {

    private val categories = listOf("Phishing", "Senhas", "Malware", "Redes Seguras", "Backup")
    private val categoryDescriptions = mapOf(
        "Phishing" to "Aprenda a identificar e se defender de ataques de phishing e engenharia social.",
        "Senhas" to "Descubra as melhores práticas para criar e gerenciar senhas fortes e seguras.",
        "Malware" to "Conheça os tipos de malware, como se proteger e manter seu sistema seguro.",
        "Redes Seguras" to "Entenda como proteger suas conexões de rede e navegar com segurança.",
        "Backup" to "Aprenda a importância do backup e as melhores estratégias para proteger seus dados."
    )
    private var currentCategory = ""
    private var isSpinning = false

    private lateinit var roletaView: RoletaView
    private lateinit var categoryText: TextView
    private lateinit var categoryDescription: TextView
    private lateinit var spinButton: Button
    private lateinit var arrowImageView: ImageView
    private lateinit var instructionText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.roleta_quiz)

        initViews()
        setupListeners()
        applyInitialAnimations()
    }

    private fun initViews() {
        roletaView = findViewById(R.id.roletaView)
        categoryText = findViewById(R.id.categoryText)
        categoryDescription = findViewById(R.id.categoryDescription)
        spinButton = findViewById(R.id.spinWheelButton)
        arrowImageView = findViewById(R.id.arrowImageView)
        instructionText = findViewById(R.id.instructionText)
    }

    private fun setupListeners() {
        spinButton.setOnClickListener {
            if (!isSpinning) {
                spinRoulette()
            } else if (currentCategory.isNotEmpty()) {
                startQuiz(currentCategory)
            }
        }
    }

    private fun applyInitialAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        fadeIn.duration = 800

        // Aplicar animação de entrada para os componentes principais
        val elements = listOf(
            findViewById<TextView>(R.id.titleText),
            findViewById<CardView>(R.id.card_category),
            findViewById<CardView>(R.id.roulette_container),
            spinButton,
            instructionText
        )

        elements.forEachIndexed { index, view ->
            Handler(Looper.getMainLooper()).postDelayed({
                view.visibility = View.VISIBLE
                view.startAnimation(fadeIn)
            }, 100L * index)
        }

        // Adicionar animação pulsante para a seta
        val pulseAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        pulseAnimation.repeatCount = Animation.INFINITE
        pulseAnimation.repeatMode = Animation.REVERSE
        pulseAnimation.duration = 1000
        arrowImageView.startAnimation(pulseAnimation)
    }

    private fun spinRoulette() {
        isSpinning = true

        categoryText.text = "A girar..."
        spinButton.isEnabled = false
        instructionText.text = "Aguarde o sorteio da categoria..."

        arrowImageView.clearAnimation()

        val randomAngle = Random.nextInt(360)
        val totalRotations = 5 * 360
        val finalAngle = totalRotations + randomAngle

        // Corrigir cálculo para refletir a posição real da seta
        val sectorAngle = 360f / categories.size
        val adjustedAngle = (360 - (finalAngle % 360) + 270) % 360
        val selectedCategoryIndex = (adjustedAngle / sectorAngle).toInt() % categories.size
        currentCategory = categories[selectedCategoryIndex]

        val valueAnimator = ValueAnimator.ofFloat(0f, finalAngle.toFloat())
        valueAnimator.duration = 4000
        valueAnimator.interpolator = AccelerateDecelerateInterpolator()

        valueAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            roletaView.setRotation(value)
        }

        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                handleRouletteStopped()
            }
        })

        valueAnimator.start()
    }


    private fun handleRouletteStopped() {
        // Animar seta para indicar a seleção
        val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce_anim) // Usa o teu XML aqui, se tiveres
        bounceAnimation.interpolator = BounceInterpolator()
        bounceAnimation.duration = 500
        arrowImageView.startAnimation(bounceAnimation)

        // Atualizar UI
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        fadeIn.duration = 500

        categoryText.text = "Categoria Selecionada: $currentCategory"

        // Mostrar descrição da categoria com animação
        categoryDescription.text = categoryDescriptions[currentCategory]
        categoryDescription.visibility = View.VISIBLE
        categoryDescription.startAnimation(fadeIn)

        // Atualizar botão
        spinButton.text = "Iniciar Quiz"
        spinButton.isEnabled = true
        instructionText.text = "Clique para começar o quiz de $currentCategory"
    }


    private fun startQuiz(category: String) {
        // Animar transição para o quiz
        val slideOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right)
        slideOut.duration = 300

        findViewById<CardView>(R.id.roulette_container).startAnimation(slideOut)

        // Iniciar o quiz após a animação
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("CATEGORY", category)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 300)
    }

}
