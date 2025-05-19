package com.example.app

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DefensorDigitalActivity : AppCompatActivity() {

    private lateinit var tvScenario: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvLevel: TextView
    private lateinit var btnOption1: Button
    private lateinit var btnOption2: Button
    private lateinit var btnOption3: Button
    private lateinit var btnOption4: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var cardView: CardView
    private lateinit var ivCharacter: ImageView
    private lateinit var feedbackCard: CardView
    private lateinit var tvFeedback: TextView

    private var currentScore = 0
    private var currentLevel = 1
    private var currentQuestion = 0
    private lateinit var questions: List<Question>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_defensor_digital)

        // Inicializar vistas
        tvScenario = findViewById(R.id.tv_scenario)
        tvScore = findViewById(R.id.tv_score)
        tvLevel = findViewById(R.id.tv_level)
        btnOption1 = findViewById(R.id.btn_option1)
        btnOption2 = findViewById(R.id.btn_option2)
        btnOption3 = findViewById(R.id.btn_option3)
        btnOption4 = findViewById(R.id.btn_option4)
        progressBar = findViewById(R.id.progress_bar)
        cardView = findViewById(R.id.card_view)
        ivCharacter = findViewById(R.id.iv_character)
        feedbackCard = findViewById(R.id.feedback_card)
        tvFeedback = findViewById(R.id.tv_feedback)

        // Configurar questões
        setupQuestions()

        // Atualizar UI
        updateUI()

        // Configurar cliques em botões
        setupButtonListeners()
    }

    private fun setupQuestions() {
        questions = listOf(
            // Nível 1: Segurança básica e senhas
            Question(
                scenario = "Recebeu um e-mail a pedir para atualizar a sua palavra-passe bancária através de uma hiperligação. O que faz?",
                options = arrayOf(
                    "Clico na hiperligação e insiro as minhas credenciais para atualizar",
                    "Ignoro o e-mail e verifico diretamente no site oficial do banco",
                    "Respondo ao e-mail a pedir mais informações",
                    "Reencaminho o e-mail para amigos para ver se é legítimo"
                ),
                correctAnswerIndex = 1,
                correctFeedback = "Correto! E-mails a solicitar dados sensíveis são frequentemente phishing. Aceda sempre ao site oficial digitando o endereço diretamente no navegador.",
                incorrectFeedback = "Cuidado! Este é um exemplo clássico de phishing. Nunca clique em hiperligações suspeitas de e-mails ou insira as suas credenciais neles.",
                level = 1
            ),

            Question(
                scenario = "Qual destas é a palavra-passe mais segura?",
                options = arrayOf(
                    "123456",
                    "password123",
                    "P@ssw0rd!2023",
                    "nomeDaMinhaEsposa"
                ),
                correctAnswerIndex = 2,
                correctFeedback = "Correto! Palavras-passe fortes combinam letras maiúsculas e minúsculas, números, símbolos e têm comprimento adequado.",
                incorrectFeedback = "Esta palavra-passe não é segura o suficiente. Uma palavra-passe forte deve conter letras (maiúsculas e minúsculas), números, símbolos e ser suficientemente longa.",
                level = 1
            ),

            // Nível 2: Privacidade e redes sociais
            Question(
                scenario = "Está a utilizar Wi-Fi público num café. Como proteger os seus dados?",
                options = arrayOf(
                    "Utilizar qualquer site normalmente",
                    "Evitar aceder a sites bancários",
                    "Utilizar uma VPN",
                    "Desativar a firewall para uma ligação mais rápida"
                ),
                correctAnswerIndex = 2,
                correctFeedback = "Correto! As VPNs encriptam o seu tráfego, protegendo os seus dados mesmo em redes Wi-Fi públicas não seguras.",
                incorrectFeedback = "Em redes Wi-Fi públicas, os seus dados podem ser intercetados. Uma VPN é a melhor proteção nestas redes.",
                level = 2
            ),

            Question(
                scenario = "Uma aplicação gratuita solicita acesso à sua localização, contactos, fotografias e microfone. O que faz?",
                options = arrayOf(
                    "Concedo todas as permissões, deve ser necessário",
                    "Avalio se a aplicação realmente precisa desses acessos para funcionar",
                    "Instalo imediatamente, todas as aplicações pedem isso",
                    "Permito o acesso e depois desativo nas definições"
                ),
                correctAnswerIndex = 1,
                correctFeedback = "Correto! Avalie sempre se uma aplicação realmente precisa de todos os acessos solicitados. Conceda apenas permissões essenciais.",
                incorrectFeedback = "Verifique sempre se as permissões são realmente necessárias. Muitas aplicações pedem mais acesso do que precisam para recolher dados.",
                level = 2
            ),

            // Nível 3: Engenharia social e ameaças avançadas
            Question(
                scenario = "Um 'técnico de suporte' telefona a informar que o seu computador está infetado e oferece ajuda. O que faz?",
                options = arrayOf(
                    "Agradeço e dou acesso ao meu computador",
                    "Forneço informações pessoais para verificação",
                    "Desligo o telefone e contacto o suporte oficial da empresa",
                    "Instalo o software que ele recomenda"
                ),
                correctAnswerIndex = 2,
                correctFeedback = "Correto! Esta é uma técnica comum de engenharia social. Verifique sempre a identidade de quem entra em contacto através dos canais oficiais.",
                incorrectFeedback = "Cuidado! Chamadas não solicitadas de 'suporte técnico' são frequentemente burlas para obter acesso ao seu dispositivo ou dados pessoais.",
                level = 3
            ),

            Question(
                scenario = "Encontra uma pen USB no parque de estacionamento da empresa. O que faz?",
                options = arrayOf(
                    "Ligo-a ao meu computador para ver o conteúdo e encontrar o dono",
                    "Ligo-a a um computador da empresa para verificar",
                    "Entrego-a ao departamento de TI ou segurança sem a ligar",
                    "Levo-a para casa para verificar mais tarde"
                ),
                correctAnswerIndex = 2,
                correctFeedback = "Correto! Pens USB abandonadas podem conter malware. Entregue-as aos profissionais de segurança sem as ligar.",
                incorrectFeedback = "Nunca ligue uma pen USB desconhecida a qualquer computador. Pode conter malware programado para infetar sistemas automaticamente.",
                level = 3
            ),

            Question(
                scenario = "Qual destas é uma medida eficaz de autenticação de dois fatores (2FA)?",
                options = arrayOf(
                    "Utilizar a mesma palavra-passe em todos os sites",
                    "Combinar palavra-passe com um código recebido no telemóvel",
                    "Mudar a sua palavra-passe a cada início de sessão",
                    "Utilizar o seu nome completo e data de nascimento"
                ),
                correctAnswerIndex = 1,
                correctFeedback = "Correto! A autenticação de dois fatores combina algo que sabe (palavra-passe) com algo que tem (como o seu telemóvel).",
                incorrectFeedback = "A autenticação de dois fatores deve combinar dois tipos diferentes de verificação - geralmente uma palavra-passe e um segundo fator como um código temporário.",
                level = 3
            ),

            // Questões bónus de nível avançado
            Question(
                scenario = "Encontra um código QR num local público que promete um prémio. O que faz?",
                options = arrayOf(
                    "Digitalizo imediatamente para ganhar o prémio",
                    "Digitalizo mas não insiro nenhuma informação pessoal",
                    "Verifico a legitimidade antes de digitalizar",
                    "Partilho com amigos para eles também ganharem"
                ),
                correctAnswerIndex = 2,
                correctFeedback = "Correto! Códigos QR em locais públicos podem ser maliciosos. Verifique sempre a fonte antes de digitalizar.",
                incorrectFeedback = "Códigos QR podem levar a sites de phishing ou iniciar transferências maliciosas. Verifique sempre a legitimidade antes.",
                level = 3
            ),

            Question(
                scenario = "Qual destas práticas NÃO ajuda a proteger contra ransomware?",
                options = arrayOf(
                    "Manter cópias de segurança regulares offline",
                    "Manter software e sistemas atualizados",
                    "Abrir todos os anexos de e-mail para verificar o seu conteúdo",
                    "Utilizar software antivírus atualizado"
                ),
                correctAnswerIndex = 2,
                correctFeedback = "Correto! Abrir anexos de e-mail desconhecidos é uma das principais formas de infeção por ransomware.",
                incorrectFeedback = "Abrir anexos de e-mail desconhecidos é muito perigoso e pode levar à infeção por ransomware e outros malwares.",
                level = 3
            )
        )
    }

    private fun setupButtonListeners() {
        btnOption1.setOnClickListener { checkAnswer(0) }
        btnOption2.setOnClickListener { checkAnswer(1) }
        btnOption3.setOnClickListener { checkAnswer(2) }
        btnOption4.setOnClickListener { checkAnswer(3) }
    }

    private fun updateUI() {
        if (currentQuestion < questions.size) {
            val currentQ = questions[currentQuestion]

            // Atualizar nível e verificar progressão
            if (currentQ.level > currentLevel) {
                currentLevel = currentQ.level
                showLevelUpDialog()
            }

            // Atualizar UI com a questão atual
            tvScenario.text = currentQ.scenario
            btnOption1.text = currentQ.options[0]
            btnOption2.text = currentQ.options[1]
            btnOption3.text = currentQ.options[2]
            btnOption4.text = currentQ.options[3]

            // Atualizar pontuação e nível
            tvScore.text = "Pontuação: $currentScore"
            tvLevel.text = "Nível: $currentLevel"

            // Atualizar a barra de progresso
            progressBar.max = questions.size
            progressBar.progress = currentQuestion

            // Animação do personagem
            animateCharacter()

        } else {
            // Fim do jogo
            showGameCompleteDialog()
        }
    }

    private fun animateCharacter() {
        ObjectAnimator.ofFloat(ivCharacter, "translationY", 0f, -20f, 0f)
            .apply {
                duration = 1000
                start()
            }
    }

    private fun checkAnswer(selectedOption: Int) {
        val currentQ = questions[currentQuestion]
        val isCorrect = selectedOption == currentQ.correctAnswerIndex

        if (isCorrect) {
            // Resposta correta
            currentScore += 10 * currentLevel
            showFeedbackDialog(true, currentQ.correctFeedback)
        } else {
            // Resposta incorreta
            showFeedbackDialog(false, currentQ.incorrectFeedback)
        }

        // Avançar para a próxima questão após delay
        Handler(Looper.getMainLooper()).postDelayed({
            currentQuestion++
            updateUI()
        }, 2000)
    }

    private fun showFeedbackDialog(isCorrect: Boolean, feedback: String) {

        feedbackCard.setCardBackgroundColor(
            if (isCorrect) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        )

        tvFeedback.text = feedback

        feedbackCard.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            feedbackCard.visibility = View.GONE
        }, 2000)
    }

    private fun showLevelUpDialog() {
        AlertDialog.Builder(this)
            .setTitle("Nível Aumentado!")
            .setMessage("Parabéns! Avanças-te para o nível $currentLevel!\n\n" +
                    "Os desafios vao ficar mais difíceis, mas estás mais preparado agora.")
            .setPositiveButton("Continuar") { _, _ -> }
            .setCancelable(false)
            .show()
    }

    private fun showGameCompleteDialog() {
        // Inicia a Activity de resultado
        val intent = Intent(this, ResultadoDefensorDigitalActivity::class.java)
        intent.putExtra("pontuacao", currentScore)
        startActivity(intent)
        finish() // Fecha esta activity
    }

    private fun resetGame() {
        currentScore = 0
        currentLevel = 1
        currentQuestion = 0
        updateUI()
    }

    // Classe para representar questões
    data class Question(
        val scenario: String,
        val options: Array<String>,
        val correctAnswerIndex: Int,
        val correctFeedback: String,
        val incorrectFeedback: String,
        val level: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Question

            if (scenario != other.scenario) return false
            if (!options.contentEquals(other.options)) return false
            if (correctAnswerIndex != other.correctAnswerIndex) return false
            if (correctFeedback != other.correctFeedback) return false
            if (incorrectFeedback != other.incorrectFeedback) return false
            if (level != other.level) return false

            return true
        }

        override fun hashCode(): Int {
            var result = scenario.hashCode()
            result = 31 * result + options.contentHashCode()
            result = 31 * result + correctAnswerIndex
            result = 31 * result + correctFeedback.hashCode()
            result = 31 * result + incorrectFeedback.hashCode()
            result = 31 * result + level
            return result
        }
    }
}