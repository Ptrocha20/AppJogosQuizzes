package com.example.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore



class FirestoreHelper(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()

    fun guardarUtilizadorNoFirestore(userId: String, nome: String, email: String, idade: Int) {
        val utilizador = hashMapOf(
            "nome" to nome,
            "email" to email,
            "idade" to idade,
            "dataNascimento" to "",  // Campo vazio para dataNascimento
            "xp" to 0L,
            "nivel" to 1,
            "quizzesConcluidos" to 0,
            "jogosConcluidos" to 0,
            "conquistasObtidas" to listOf<String>()
        )

        /*db.collection("Utilizadores").document(userId)
            .set(utilizador)
            .addOnSuccessListener {
                Toast.makeText(context, "Guardado no Firestore!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Erro ao guardar utilizador: ${e.message}", Toast.LENGTH_SHORT).show()
            }*/
    }

    fun verificarEGuardarUtilizador(userId: String, nome: String, email: String) {
        // Primeiro, verificar se o usuário já existe
        db.collection("Utilizadores").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Usuário já existe, atualizar apenas os dados básicos de nome e email
                    // Preservando idade, data de nascimento e dados de progresso
                    val idadeAtual = document.getLong("idade")?.toInt() ?: 0
                    val dataNascimento = document.getString("dataNascimento") ?: ""
                    val xpAtual = document.getLong("xp") ?: 0L
                    val nivelAtual = document.getLong("nivel")?.toInt() ?: 1

                    // Recuperar os dados de progresso de quizzes e jogos
                    val quizzesConcluidos = document.getLong("quizzesConcluidos")?.toInt() ?: 0
                    val jogosConcluidos = document.getLong("jogosConcluidos")?.toInt() ?: 0

                    // Recuperar a lista de conquistas
                    @Suppress("UNCHECKED_CAST")
                    val conquistasObtidas = document.get("conquistasObtidas") as? List<String> ?: listOf()

                    // Recuperar URL da imagem de perfil, se existir
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""

                    val dadosAtualizados = hashMapOf(
                        "nome" to nome,
                        "email" to email,
                        "idade" to idadeAtual,
                        "dataNascimento" to dataNascimento,
                        "xp" to xpAtual,
                        "nivel" to nivelAtual,
                        "quizzesConcluidos" to quizzesConcluidos,
                        "jogosConcluidos" to jogosConcluidos,
                        "conquistasObtidas" to conquistasObtidas,
                        "profileImageUrl" to profileImageUrl
                    )

                    db.collection("Utilizadores").document(userId)
                        .set(dadosAtualizados)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Dados atualizados!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Erro ao atualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Novo usuário, criar com valores iniciais
                    guardarUtilizadorNoFirestore(userId, nome, email, 0)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Erro ao verificar usuário: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var isPasswordVisible = false
    private lateinit var callbackManager: CallbackManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.login)


        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()
        setupGoogleSignIn()
        setupLoginButton()
        setupPasswordVisibilityToggle()


        callbackManager = CallbackManager.Factory.create()

        findViewById<TextView>(R.id.textView4).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        findViewById<TextView>(R.id.textView8).setOnClickListener {
            startActivity(Intent(this, RecuperarSenhaActivity::class.java))
        }
    }


    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<ImageView>(R.id.googleLoginBtn).setOnClickListener {
            // Faz logout apenas do Google antes de iniciar um novo login
            signOutGoogle()

            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }


    private fun setupLoginButton() {
        findViewById<Button>(R.id.button3).setOnClickListener {
            val emailInput = findViewById<EditText>(R.id.edit_email).text.toString().trim()
            val senhaInput = findViewById<EditText>(R.id.edit_palavra_passe).text.toString().trim()

            if (emailInput.isEmpty() || senhaInput.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            realizarLogin(emailInput, senhaInput)
        }
    }

    private fun signOutGoogle() {
        googleSignInClient.signOut().addOnCompleteListener {
            // Removido o logout do Firebase para preservar os dados de autenticação
        }
    }

    private fun realizarLogin(email: String, senha: String) {

        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                    navegarParaProximaActivity()
                } else {
                    Toast.makeText(this, "E-mail ou senha incorretos.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navegarParaProximaActivity() {
        startActivity(Intent(this, ConfiguracaoMFAActivity::class.java))
        finish()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPasswordVisibilityToggle() {
        val editPalavraPasse = findViewById<EditText>(R.id.edit_palavra_passe)

        editPalavraPasse.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = editPalavraPasse.compoundDrawablesRelative[2] ?: return@setOnTouchListener false

                // Verifica se o clique foi no ícone
                if (event.rawX >= (editPalavraPasse.right - drawableEnd.bounds.width() - editPalavraPasse.paddingEnd)) {
                    isPasswordVisible = !isPasswordVisible

                    // Alterna entre mostrar e esconder a senha
                    if (isPasswordVisible) {
                        editPalavraPasse.transformationMethod = null
                    } else {
                        editPalavraPasse.transformationMethod = PasswordTransformationMethod.getInstance()
                    }

                    // Atualiza o ícone do olho
                    val icon = if (isPasswordVisible) R.drawable.ic_eye else R.drawable.ic_eye_closed
                    editPalavraPasse.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, icon), null)

                    // Move o cursor para o fim
                    editPalavraPasse.setSelection(editPalavraPasse.text.length)

                    v.performClick()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Erro ao autenticar com Google: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().useAppLanguage()

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val user = auth.currentUser
                    if (user != null) {
                        // Usa o método modificado que não passa mais a idade como parâmetro
                        FirestoreHelper(this).verificarEGuardarUtilizador(
                            userId!!,
                            user.displayName ?: "Nome Desconhecido",
                            user.email ?: "Email Desconhecido"
                        )
                    }
                    navegarParaProximaActivity()
                } else {
                    Toast.makeText(this, "Falha ao autenticar com Google!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}