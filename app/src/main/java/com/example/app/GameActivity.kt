package com.example.app

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit

class GameActivity : AppCompatActivity() {

    private lateinit var scoreTextView: TextView
    private lateinit var levelTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var gameLayout: FrameLayout
    private lateinit var phraseContainer: LinearLayout
    private lateinit var lifeContainer: LinearLayout
    private val handler = Handler(Looper.getMainLooper())
    private var email: String? = null
    private var mediaPlayer: MediaPlayer? = null
    private var attackTimer: CountDownTimer? = null
    private var gameTimer: CountDownTimer? = null
    private var timeRemaining: Long = GAME_DURATION
    private var layoutReady = false

    // Lista expandida de frases com categorias
    private val securityPhrases = listOf(
        SecurityPhrase("Uma palavra-passe complexa é essencial para a segurança da sua conta.", true, "PASSWORDS"),
        SecurityPhrase("Uma palavra-passe simples é suficiente para proteger a sua conta.", false, "PASSWORDS"),
        SecurityPhrase("Utilize autenticação de dois fatores sempre que possível.", true, "AUTH"),
        SecurityPhrase("Verifique a sua conta uma vez por mês.", true, "MAINTENANCE"),
        SecurityPhrase("Nunca partilhe a sua palavra-passe com ninguém.", true, "PASSWORDS"),
        SecurityPhrase("A mesma palavra-passe para várias contas é uma boa prática.", false, "PASSWORDS"),
        SecurityPhrase("Mude a sua palavra-passe todas as semanas.", false, "MAINTENANCE"),
        SecurityPhrase("Evite clicar em hiperligações suspeitas para manter a sua conta segura.", true, "PHISHING"),
        SecurityPhrase("É seguro usar redes Wi-Fi públicas para acessar suas contas bancárias.", false, "NETWORKS"),
        SecurityPhrase("Verificar o URL antes de inserir credenciais é uma boa prática de segurança.", true, "PHISHING"),
        SecurityPhrase("Anexos de e-mail de remetentes desconhecidos são geralmente seguros para abrir.", false, "PHISHING"),
        SecurityPhrase("Backups regulares ajudam a proteger contra ransomware.", true, "MAINTENANCE"),
        SecurityPhrase("Manter software atualizado é importante para a segurança.", true, "MAINTENANCE"),
        SecurityPhrase("Senhas como '123456' ou 'password' são difíceis de adivinhar.", false, "PASSWORDS"),
        SecurityPhrase("Uma frase-passe longa é mais segura que uma senha curta com caracteres especiais.", true, "PASSWORDS"),
        SecurityPhrase("Programas antivírus são desnecessários se você for cuidadoso online.", false, "PROTECTION")
    )

    private var currentPhraseIndex = 0
    private var score = 0
    private var level = 1
    private var lives = MAX_LIVES
    private var attackSpeed = BASE_ATTACK_SPEED
    private var activeAttacks = 0
    private var correctAnswersInRow = 0

    companion object {
        const val MAX_ATTACKS = 5
        const val MAX_LIVES = 3
        const val BASE_ATTACK_SPEED = 5000L  // Velocidade base em milissegundos
        const val MIN_ATTACK_SPEED = 1500L   // Velocidade máxima (menor tempo)
        const val ATTACK_SPEED_DECREASE = 300L // Quanto a velocidade diminui por nível
        const val POINTS_PER_CORRECT = 10
        const val POINTS_PER_INCORRECT = -5
        const val POINTS_PER_BLOCKED_ATTACK = 5
        const val BONUS_POINTS_STREAK = 5
        const val LEVEL_UP_THRESHOLD = 50
        const val GAME_DURATION = 120000L    // 2 minutos em milissegundos
        const val DEFAULT_SIZE = 100 // Default size for attacks if layout not measured yet
    }

    data class SecurityPhrase(val text: String, val isCorrect: Boolean, val category: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_game_defesa)

        // Inicializar vistas
        initializeViews()

        // Obter dados da intent
        email = intent.getStringExtra("EMAIL")

        // Configurar jogo após o layout ser medido
        gameLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (gameLayout.width > 0 && gameLayout.height > 0 && !layoutReady) {
                    layoutReady = true
                    gameLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    setupGame()
                    startGameTimer()
                }
            }
        })
    }

    private fun initializeViews() {
        scoreTextView = findViewById(R.id.scoreText)
        levelTextView = findViewById(R.id.levelText)
        timerTextView = findViewById(R.id.timerText)
        gameLayout = findViewById(R.id.gameLayout)
        phraseContainer = findViewById(R.id.phraseContainer)
        lifeContainer = findViewById(R.id.lifeContainer)

        // Configurar botões de resposta
        findViewById<Button>(R.id.buttonAnswer1).setOnClickListener {
            checkAnswer(true)
        }
        findViewById<Button>(R.id.buttonAnswer2).setOnClickListener {
            checkAnswer(false)
        }

        // Configurar botão de pausa
        findViewById<ImageButton>(R.id.pauseButton).setOnClickListener {
            pauseGame()
        }

        // Inicializar vidas
        updateLivesDisplay()
    }

    private fun setupGame() {
        // Embaralhar frases
        securityPhrases.shuffled().toMutableList()

        // Mostrar primeira frase
        showSecurityPhrase()

        // Atualizar UI
        updateScore()
        updateLevel()

        // Iniciar ataques
        startAttackTimer()
    }

    private fun startGameTimer() {
        gameTimer = object : CountDownTimer(GAME_DURATION, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                updateTimerDisplay(millisUntilFinished)
            }

            override fun onFinish() {
                finishGame()
            }
        }.start()
    }

    private fun updateTimerDisplay(millisUntilFinished: Long) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                TimeUnit.MINUTES.toSeconds(minutes)
        timerTextView.text = String.format("%02d:%02d", minutes, seconds)

        // Mudar cor quando estiver acabando o tempo
        if (millisUntilFinished < 30000) { // Menos de 30 segundos
            timerTextView.setTextColor(Color.RED)
            // Piscar o temporizador
            if (millisUntilFinished % 2000 < 1000) {
                ObjectAnimator.ofFloat(timerTextView, "alpha", 1f, 0.3f).apply {
                    duration = 500
                    repeatCount = 1
                    repeatMode = ValueAnimator.REVERSE
                    start()
                }
            }
        }
    }

    private fun startAttackTimer() {
        attackTimer?.cancel()

        // Ajusta a velocidade baseada no nível
        val currentSpeed = maxOf(BASE_ATTACK_SPEED - (level - 1) * ATTACK_SPEED_DECREASE, MIN_ATTACK_SPEED)

        attackTimer = object : CountDownTimer(GAME_DURATION, currentSpeed) {
            override fun onTick(millisUntilFinished: Long) {
                if (activeAttacks < MAX_ATTACKS && layoutReady) {
                    launchAttack()
                }
            }

            override fun onFinish() {
                // Nada a fazer, o timer do jogo cuidará do fim
            }
        }.start()
    }

    private fun showSecurityPhrase() {
        if (currentPhraseIndex < securityPhrases.size) {
            val currentPhrase = securityPhrases[currentPhraseIndex]

            // Destaque visual baseado na categoria
            val categoryColor = when (currentPhrase.category) {
                "PASSWORDS" -> "#3F51B5"  // Azul índigo
                "AUTH" -> "#4CAF50"       // Verde
                "PHISHING" -> "#F44336"   // Vermelho
                "NETWORKS" -> "#FF9800"   // Laranja
                "MAINTENANCE" -> "#9C27B0" // Roxo
                "PROTECTION" -> "#009688" // Verde-azulado
                else -> "#000000"         // Preto (padrão)
            }

            val phraseTextView = findViewById<TextView>(R.id.securityPhraseText)
            phraseTextView.text = currentPhrase.text
            phraseTextView.setTextColor(Color.parseColor(categoryColor))

            // Animação de entrada
            phraseTextView.alpha = 0f
            phraseTextView.animate()
                .alpha(1f)
                .setDuration(300)
                .start()

            // Mostrar categoria em um chip
            val categoryChip = findViewById<TextView>(R.id.categoryChip)
            categoryChip.text = currentPhrase.category
            categoryChip.setBackgroundColor(Color.parseColor(categoryColor))
            categoryChip.visibility = View.VISIBLE
        } else {
            // Reiniciar frases se todas já foram usadas
            currentPhraseIndex = 0
            securityPhrases.shuffled().toMutableList()
            showSecurityPhrase()
        }
    }

    private fun checkAnswer(isAnswerCorrect: Boolean) {
        val currentPhrase = securityPhrases[currentPhraseIndex]
        val resultView = findViewById<TextView>(R.id.resultText)

        if (isAnswerCorrect == currentPhrase.isCorrect) {
            // Resposta correta
            score += POINTS_PER_CORRECT
            correctAnswersInRow++

            // Bônus por sequência de acertos
            if (correctAnswersInRow >= 3) {
                score += BONUS_POINTS_STREAK
                showFloatingText("+$BONUS_POINTS_STREAK BÔNUS!", Color.GREEN)
            }

            resultView.text = "CORRETO! +$POINTS_PER_CORRECT"
            resultView.setTextColor(Color.GREEN)

            // Bloquear um ataque
            blockRandomAttack()
        } else {
            // Resposta incorreta
            score = maxOf(0, score + POINTS_PER_INCORRECT)  // Não permitir pontuação negativa
            correctAnswersInRow = 0

            resultView.text = "INCORRETO! $POINTS_PER_INCORRECT"
            resultView.setTextColor(Color.RED)
        }

        // Mostrar resultado brevemente
        resultView.visibility = View.VISIBLE
        handler.postDelayed({
            resultView.visibility = View.INVISIBLE
        }, 1500)

        updateScore()
        checkLevelUp()
        loadNextPhrase()
    }

    private fun blockRandomAttack() {
        // Método correto para encontrar todas as views com a tag específica
        val allViews = ArrayList<View>()
        findViewsWithTag(gameLayout, "activeAttack", allViews)

        // Filtrar apenas as ImageViews
        val attacks = allViews.filterIsInstance<ImageView>()

        if (attacks.isNotEmpty()) {
            val attackToBlock = attacks.random()

            // Animação de bloqueio
            val shield = ImageView(this).apply {
                setImageResource(R.drawable.ic_shield)
                layoutParams = attackToBlock.layoutParams
                scaleX = 0f
                scaleY = 0f
            }

            gameLayout.addView(shield)

            // Animação do escudo
            shield.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .alpha(0f)
                .setDuration(500)
                .withEndAction {
                    gameLayout.removeView(shield)
                    gameLayout.removeView(attackToBlock)
                    activeAttacks--

                    // Pontos por bloquear ataque
                    score += POINTS_PER_BLOCKED_ATTACK
                    updateScore()
                    showFloatingText("+$POINTS_PER_BLOCKED_ATTACK", Color.CYAN)
                }
                .start()
        }
    }

    private fun findViewsWithTag(root: View, tag: Any, results: ArrayList<View>) {
        if (tag == root.tag) {
            results.add(root)
        }

        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                findViewsWithTag(root.getChildAt(i), tag, results)
            }
        }
    }

    private fun showFloatingText(text: String, color: Int) {
        val floatingText = TextView(this).apply {
            setText(text)
            setTextColor(color)
            textSize = 20f
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = (gameLayout.width / 2) - 50
                topMargin = (gameLayout.height / 2) - 50
            }
        }

        gameLayout.addView(floatingText)

        // Animação flutuante
        ObjectAnimator.ofFloat(floatingText, "translationY", 0f, -150f).apply {
            duration = 1000
            interpolator = AccelerateInterpolator()
            start()
        }

        ObjectAnimator.ofFloat(floatingText, "alpha", 1f, 0f).apply {
            duration = 1000
            startDelay = 500
            start()
        }

        // Remover após a animação
        handler.postDelayed({
            gameLayout.removeView(floatingText)
        }, 1500)
    }

    private fun updateScore() {
        scoreTextView.text = "Pontuação: $score"
    }

    private fun updateLevel() {
        levelTextView.text = "Nível: $level"
    }

    private fun updateLivesDisplay() {
        lifeContainer.removeAllViews()

        // Adicionar ícones de vida
        for (i in 1..MAX_LIVES) {
            val lifeIcon = ImageView(this).apply {
                setImageResource(
                    if (i <= lives) R.drawable.ic_heart_full else R.drawable.ic_heart_empty
                )
                layoutParams = LinearLayout.LayoutParams(48, 48).apply {
                    marginEnd = 8
                }
            }
            lifeContainer.addView(lifeIcon)
        }
    }

    private fun checkLevelUp() {
        if (score >= level * LEVEL_UP_THRESHOLD) {
            level++
            updateLevel()

            // Animação de nível aumentado
            val levelUpText = TextView(this).apply {
                text = "NÍVEL $level!"
                setTextColor(Color.YELLOW)
                textSize = 30f
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.CENTER
                }
            }

            gameLayout.addView(levelUpText)

            // Animação
            ObjectAnimator.ofFloat(levelUpText, "scaleX", 0f, 1.5f).apply {
                duration = 500
                repeatCount = 1
                repeatMode = ValueAnimator.REVERSE
                start()
            }

            ObjectAnimator.ofFloat(levelUpText, "scaleY", 0f, 1.5f).apply {
                duration = 500
                repeatCount = 1
                repeatMode = ValueAnimator.REVERSE
                start()
            }

            // Remover após a animação
            handler.postDelayed({
                gameLayout.removeView(levelUpText)
            }, 1500)

            // Ajustar dificuldade
            startAttackTimer() // Reinicia o timer com velocidade atualizada

        }
    }

    private fun loadNextPhrase() {
        currentPhraseIndex++
        if (currentPhraseIndex >= securityPhrases.size) {
            // Reiniciar frases se todas já foram usadas
            currentPhraseIndex = 0
            securityPhrases.shuffled().toMutableList()
        }
        showSecurityPhrase()
    }

    private fun launchAttack() {
        if (!layoutReady || gameLayout.width <= 0 || gameLayout.height <= 0) {
            return // Não lançar ataques se o layout não estiver pronto
        }

        activeAttacks++

        // Criar ataque com imagem aleatória
        val attackImages = listOf(
            R.drawable.ic_virus,
            R.drawable.ic_hacker,
            R.drawable.ic_trojan,
            R.drawable.ic_phishing
        )

        val attackType = attackImages.random()

        val attackImage = ImageView(this).apply {
            setImageResource(attackType)
            layoutParams = FrameLayout.LayoutParams(100, 100).apply {
                val startPosition = getRandomStartPosition()
                topMargin = startPosition.first
                leftMargin = startPosition.second
            }
            tag = "activeAttack"

            // Sombra para efeito 3D
            elevation = 10f
        }

        gameLayout.addView(attackImage)
        moveAttack(attackImage)
    }

    private fun getRandomStartPosition(): Pair<Int, Int> {
        // Verificar se o layout está pronto
        if (gameLayout.width <= 0 || gameLayout.height <= 0) {
            return Pair(0, 0) // Posição padrão segura se o layout não estiver pronto
        }

        // Determinar uma posição aleatória para o ataque nas bordas da tela
        val side = (0..3).random() // 0: topo, 1: direita, 2: baixo, 3: esquerda
        val maxWidth = maxOf(1, gameLayout.width - 100)  // Garantir que seja pelo menos 1
        val maxHeight = maxOf(1, gameLayout.height - 100)  // Garantir que seja pelo menos 1

        return when (side) {
            0 -> Pair(0, (0 until maxWidth).random()) // Topo
            1 -> Pair((0 until maxHeight).random(), maxOf(0, maxWidth)) // Direita
            2 -> Pair(maxOf(0, maxHeight), (0 until maxWidth).random()) // Baixo
            else -> Pair((0 until maxHeight).random(), 0) // Esquerda
        }
    }

    private fun moveAttack(attackImage: ImageView) {
        // Velocidade base ajustada pelo nível
        val moveSpeed = maxOf(50 - (level * 3), 20) // Limite inferior de 20ms

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!gameLayout.isAttachedToWindow) return

                val accountProtectionArea = findViewById<FrameLayout>(R.id.accountProtectionArea)
                val accountLocation = intArrayOf(0, 0)
                accountProtectionArea.getLocationInWindow(accountLocation)

                val attackLocation = intArrayOf(0, 0)
                attackImage.getLocationInWindow(attackLocation)

                // Calcular direção para o alvo
                val deltaX = accountLocation[0] - attackLocation[0]
                val deltaY = accountLocation[1] - attackLocation[1]

                // Normalizar e aplicar velocidade
                val distance = Math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble())
                if (distance > 5) {
                    // Velocidade aumenta com o nível
                    val speedMultiplier = 1 + (level * 0.15f)
                    val stepX = (deltaX / distance * 10 * speedMultiplier).toInt()
                    val stepY = (deltaY / distance * 10 * speedMultiplier).toInt()

                    // Mover o ataque
                    val layoutParams = attackImage.layoutParams as FrameLayout.LayoutParams
                    layoutParams.leftMargin += stepX
                    layoutParams.topMargin += stepY
                    attackImage.layoutParams = layoutParams

                    // Rotação para efeito visual
                    attackImage.rotation += 5f
                }

                // Verificar colisão
                if (checkCollision(accountLocation, attackLocation)) {
                    onAttackHit(attackImage)
                } else if (attackLocation[1] > gameLayout.height ||
                    attackLocation[0] < 0 ||
                    attackLocation[0] > gameLayout.width) {
                    // Ataque saiu da tela
                    resetAttack(attackImage)
                } else {
                    // Continuar movimento
                    handler.postDelayed(this, moveSpeed.toLong())
                }
            }
        }, moveSpeed.toLong())
    }

    private fun checkCollision(accountLocation: IntArray, attackLocation: IntArray): Boolean {
        val accountArea = findViewById<FrameLayout>(R.id.accountProtectionArea)

        val accountLeft = accountLocation[0]
        val accountTop = accountLocation[1]
        val accountRight = accountLeft + accountArea.width
        val accountBottom = accountTop + accountArea.height

        val attackLeft = attackLocation[0]
        val attackTop = attackLocation[1]
        val attackRight = attackLeft + 100
        val attackBottom = attackTop + 100

        return !(attackLeft > accountRight || attackRight < accountLeft ||
                attackTop > accountBottom || attackBottom < accountTop)
    }

    private fun onAttackHit(attackImage: ImageView) {
        // Remover o ataque
        gameLayout.removeView(attackImage)
        activeAttacks--

        // Mostrar animação de impacto
        val impactView = ImageView(this).apply {
            setImageResource(R.drawable.ic_impact)
            layoutParams = attackImage.layoutParams
            scaleX = 0f
            scaleY = 0f
        }

        gameLayout.addView(impactView)

        // Animação de impacto
        impactView.animate()
            .scaleX(2f)
            .scaleY(2f)
            .alpha(0f)
            .setDuration(500)
            .withEndAction {
                gameLayout.removeView(impactView)
            }
            .start()

        // Reduzir vida
        decreaseLife()
    }

    private fun decreaseLife() {
        lives--
        updateLivesDisplay()

        // Vibrar a tela
        val accountArea = findViewById<FrameLayout>(R.id.accountProtectionArea)
        ObjectAnimator.ofFloat(accountArea, "translationX", 0f, 20f, -20f, 20f, -20f, 10f, -10f, 5f, -5f, 0f).apply {
            duration = 500
            start()
        }

        // Verificar fim de jogo
        if (lives <= 0) {
            handler.postDelayed({
                finishGame()
            }, 1000)
        }
    }

    private fun resetAttack(attackImage: ImageView) {
        // Remover ataque
        gameLayout.removeView(attackImage)
        activeAttacks--
    }

    private fun pauseGame() {
        // Pausar temporizadores
        gameTimer?.cancel()
        attackTimer?.cancel()

        // Pausar todos os handlers
        handler.removeCallbacksAndMessages(null)

        // Pausar música
        mediaPlayer?.pause()

        // Mostrar diálogo de pausa
        val pauseDialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("Jogo Pausado")
            .setMessage("Pontuação atual: $score\nTempo restante: ${timerTextView.text}")
            .setCancelable(false)
            .setPositiveButton("Continuar") { dialog, _ ->
                dialog.dismiss()
                resumeGame()
            }
            .setNegativeButton("Sair") { _, _ ->
                finishGame()
            }
            .create()

        pauseDialog.show()
    }

    private fun resumeGame() {
        // Reiniciar temporizador do jogo
        gameTimer = object : CountDownTimer(timeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = millisUntilFinished
                updateTimerDisplay(millisUntilFinished)
            }

            override fun onFinish() {
                finishGame()
            }
        }.start()

        // Reiniciar temporizador de ataques
        startAttackTimer()

        // Retomar música
        mediaPlayer?.start()
    }

    private fun finishGame() {
        // Cancelar temporizadores
        gameTimer?.cancel()
        attackTimer?.cancel()
        handler.removeCallbacksAndMessages(null)

        // Parar música
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        // Iniciar atividade de resultado
        val intent = Intent(this, ResultadoActivityJogoDefesaRede::class.java)
        intent.putExtra("FINAL_SCORE", score)
        intent.putExtra("EMAIL", email)
        intent.putExtra("LEVEL", level)
        intent.putExtra("LIVES", lives)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        gameTimer?.cancel()
        attackTimer?.cancel()
        handler.removeCallbacksAndMessages(null)
    }
}