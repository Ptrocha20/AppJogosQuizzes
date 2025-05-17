package com.example.app

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class QuizActivity : AppCompatActivity() {

    private lateinit var questionText: TextView
    private lateinit var option1Button: Button
    private lateinit var option2Button: Button
    private lateinit var option3Button: Button
    private lateinit var option4Button: Button
    private lateinit var nextButton: Button
    private lateinit var resultText: TextView
    private lateinit var correctAnswerText: TextView
    private lateinit var cardResult: CardView
    private lateinit var progressBar: ProgressBar
    private lateinit var questionCounterText: TextView
    private lateinit var categoryText: TextView

    private val category: String by lazy { intent.getStringExtra("CATEGORY") ?: "Phishing" }

    private var currentQuestionIndex = 0
    private var score = 0
    private val totalQuestions = 4

    private val questions = mapOf(
        "Phishing" to listOf(
            Question("O que é Phishing?", listOf("Ataque de engenharia social", "Tipo de malware", "Ataque de rede", "Nenhuma das opções"), 0),
            Question("Qual é um sinal de phishing?", listOf("Email de fontes desconhecidas", "Link para uma página segura", "Senha forte", "Todos os itens"), 0),
            Question("O que pode ser uma técnica de phishing?", listOf("Falsificação de identidade", "Criação de senhas fortes", "Autenticação multifatorial", "Backup de dados"), 0),
            Question("Como prevenir phishing?", listOf("Desconfiar de e-mails suspeitos", "Fazer backup regularmente", "Usar senhas fracas", "Compartilhar senhas com amigos"), 0)
        ),
        "Senhas" to listOf(
            Question("Qual é a melhor prática para senhas?", listOf("Usar senhas fortes", "Usar a mesma senha em todos os sites", "Compartilhar com amigos", "Escrever em papel"), 0),
            Question("Qual desses é um exemplo de senha forte?", listOf("P@ssw0rd123", "12345", "senha", "abcdef"), 0),
            Question("Qual é a melhor forma de armazenar senhas?", listOf("Gestor de senhas", "Anotar no papel", "Repetir a mesma senha", "Não armazenar senhas"), 0),
            Question("O que fazer se esquecer a senha?", listOf("Redefinir a senha", "Criar outra conta", "Usar senha fraca", "Compartilhar com amigos"), 0)
        ),
        "Malware" to listOf(
            Question("O que é um malware?", listOf("Software malicioso", "Programa de segurança", "Antivírus", "Sistema operacional"), 0),
            Question("Qual é o objetivo de um vírus?", listOf("Se espalhar e danificar", "Proteger o sistema", "Acelerar a navegação", "Aumentar a memória"), 0),
            Question("Como prevenir infecção por malware?", listOf("Evitar clicar em links desconhecidos", "Instalar softwares piratas", "Usar senhas simples", "Desabilitar antivírus"), 0),
            Question("O que é um spyware?", listOf("Programa que monitora atividades", "Antivírus", "Backup de dados", "Sistema operativo"), 0)
        ),
        "Redes Seguras" to listOf(
            Question("O que é uma rede segura?", listOf("Rede com criptografia", "Rede aberta", "Rede pública", "Rede sem senha"), 0),
            Question("Qual é o melhor método de autenticação?", listOf("Autenticação multifatorial", "Senha simples", "Usar Wi-Fi público", "Ignorar antivírus"), 0),
            Question("O que é VPN?", listOf("Rede privada virtual", "Rede pública", "Sistema de backup", "Firewall"), 0),
            Question("Como garantir a segurança em redes Wi-Fi?", listOf("Usar senhas fortes", "Deixar sem senha", "Compartilhar a senha com todos", "Usar rede pública sem proteção"), 0)
        ),
        "Backup" to listOf(
            Question("O que é backup?", listOf("Cópia de segurança de dados", "Aumento de espaço no disco", "Desfragmentação de disco", "Eliminação de arquivos"), 0),
            Question("Como fazer backup dos dados?", listOf("Usar serviço de nuvem", "Deixar os arquivos sem proteção", "Usar apenas dispositivos físicos", "Excluir dados antigos"), 0),
            Question("Qual é a melhor frequência para backup?", listOf("Diariamente ou semanalmente", "A cada mês", "Somente quando o computador está lento", "Nunca"), 0),
            Question("Qual é o risco de não fazer backup?", listOf("Perda de dados", "Mais espaço no disco", "Maior desempenho", "Menos risco de vírus"), 0)
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_quiz)

        initViews()
        setupListeners()
        setupInitialUI()
        loadQuestion()
    }

    private fun initViews() {
        questionText = findViewById(R.id.questionText)
        option1Button = findViewById(R.id.option1Button)
        option2Button = findViewById(R.id.option2Button)
        option3Button = findViewById(R.id.option3Button)
        option4Button = findViewById(R.id.option4Button)
        nextButton = findViewById(R.id.nextButton)
        resultText = findViewById(R.id.resultText)
        correctAnswerText = findViewById(R.id.correctAnswerText)
        cardResult = findViewById(R.id.card_result)
        progressBar = findViewById(R.id.progressBar)
        questionCounterText = findViewById(R.id.tv_question_counter)
        categoryText = findViewById(R.id.tv_category)
    }

    private fun setupListeners() {
        option1Button.setOnClickListener { checkAnswer(0) }
        option2Button.setOnClickListener { checkAnswer(1) }
        option3Button.setOnClickListener { checkAnswer(2) }
        option4Button.setOnClickListener { checkAnswer(3) }
        nextButton.setOnClickListener { nextQuestion() }
    }

    private fun setupInitialUI() {
        categoryText.text = category
        progressBar.max = totalQuestions
        progressBar.progress = 1
    }

    private fun loadQuestion() {
        val questionList = questions[category] ?: return
        val question = questionList.getOrNull(currentQuestionIndex) ?: return

        // Atualizar contador de perguntas
        questionCounterText.text = "Pergunta ${currentQuestionIndex + 1} de $totalQuestions"
        progressBar.progress = currentQuestionIndex + 1

        // Animar a entrada da pergunta
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        questionText.startAnimation(fadeIn)
        questionText.text = question.text

        // Configurar opções
        val options = listOf(option1Button, option2Button, option3Button, option4Button)
        options.forEachIndexed { index, button ->
            button.text = question.options[index]
            button.startAnimation(fadeIn)
        }

        resetButtonStyles()

        // Verifica se está na última pergunta e altera o texto do botão
        if (currentQuestionIndex == (questionList.size - 1)) {
            nextButton.text = "Terminar Quiz"
        } else {
            nextButton.text = "Próxima Pergunta"
        }

        nextButton.visibility = View.GONE
        cardResult.visibility = View.GONE
    }

    private fun checkAnswer(selectedOption: Int) {
        val questionList = questions[category] ?: return
        val question = questionList.getOrNull(currentQuestionIndex) ?: return

        val isCorrect = selectedOption == question.correctOption
        if (isCorrect) {
            score++
            resultText.text = "Resposta Certa!"
            resultText.setTextColor(Color.GREEN)
            correctAnswerText.visibility = View.GONE
        } else {
            resultText.text = "Resposta Errada!"
            resultText.setTextColor(Color.RED)
            correctAnswerText.text = "Resposta correta: ${question.options[question.correctOption]}"
            correctAnswerText.visibility = View.VISIBLE
        }

        highlightSelectedAnswer(selectedOption, isCorrect)

        // Mostrar o resultado com animação
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        cardResult.visibility = View.VISIBLE
        cardResult.startAnimation(fadeIn)
        nextButton.visibility = View.VISIBLE
        nextButton.startAnimation(fadeIn)
    }

    private fun highlightSelectedAnswer(selectedOption: Int, isCorrect: Boolean) {
        val buttons = listOf(option1Button, option2Button, option3Button, option4Button)

        // Desabilitar todos os botões
        buttons.forEach { it.isEnabled = false }

        // Destacar apenas a opção selecionada com animação
        val button = buttons[selectedOption]
        val colorFrom = Color.parseColor("#193180")
        val colorTo = if (isCorrect) Color.GREEN else Color.RED

        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 300
        colorAnimation.addUpdateListener { animator ->
            button.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()

    }

    private fun resetButtonStyles() {
        val buttons = listOf(option1Button, option2Button, option3Button, option4Button)
        buttons.forEach { button ->
            button.setBackgroundColor(Color.parseColor("#193180"))
            button.isEnabled = true
        }
    }

    private fun nextQuestion() {
        currentQuestionIndex++
        if (currentQuestionIndex < (questions[category]?.size ?: 0)) {
            // Fade out dos elementos atuais
            val fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
            fadeOut.duration = 200

            val views = listOf(questionText, option1Button, option2Button, option3Button, option4Button, cardResult, nextButton)
            views.forEach { it.startAnimation(fadeOut) }

            // Carregar nova questão após a animação
            Handler(Looper.getMainLooper()).postDelayed({
                loadQuestion()
            }, 200)
        } else {
            showResults()
        }
    }

    private fun showResults() {
        val intent = Intent(this, ResultadoActivity::class.java)
        intent.putExtra("SCORE", score)
        intent.putExtra("TOTAL_QUESTIONS", totalQuestions)
        intent.putExtra("CATEGORY", category)

        // Adicionar transição de saída
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}

data class Question(val text: String, val options: List<String>, val correctOption: Int)