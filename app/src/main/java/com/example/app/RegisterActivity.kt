package com.example.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.registar)


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        setupRegisterButton()


        setupPasswordVisibilityToggle()

        val loginTextView = findViewById<TextView>(R.id.textView6)
        loginTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRegisterButton() {
        findViewById<Button>(R.id.button4).setOnClickListener {
            val nomeInput = findViewById<EditText>(R.id.nome_escrito).text.toString().trim()
            val emailInput = findViewById<EditText>(R.id.email_escrito).text.toString().trim()
            val senhaInput = findViewById<EditText>(R.id.palavra_passe_escrita).text.toString().trim()
            val idadeInput = findViewById<EditText>(R.id.idade_escrita).text.toString().trim()

            if (nomeInput.isEmpty() || emailInput.isEmpty() || senhaInput.isEmpty() || idadeInput.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val idade = idadeInput.toIntOrNull()
            if (idade == null || idade <= 0) {
                Toast.makeText(this, "Por favor, insira uma idade válida.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            registarUtilizador(nomeInput, emailInput, senhaInput, idade)
        }
    }

    private fun registarUtilizador(nome: String, email: String, senha: String, idade: Int) {
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        guardarUtilizadorNoFirestore(userId, nome, email, idade)
                    }
                } else {
                    Toast.makeText(this, "Erro ao registar utilizador: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun guardarUtilizadorNoFirestore(userId: String, nome: String, email: String, idade: Int) {
        val db = FirebaseFirestore.getInstance()
        val utilizador = hashMapOf(
            "nome" to nome,
            "email" to email,
            "idade" to idade,
            "xp" to 0L,
            "nivel" to 1
        )

        db.collection("Utilizadores").document(userId)
            .set(utilizador)
            .addOnSuccessListener {
                Toast.makeText(this, "Registo bem-sucedido!", Toast.LENGTH_SHORT).show()
                limparCampos()

                // Direciona para a página de login
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish() // Fecha a RegisterActivity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao guardar utilizador: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun limparCampos() {
        findViewById<EditText>(R.id.nome_escrito).text.clear()
        findViewById<EditText>(R.id.email_escrito).text.clear()
        findViewById<EditText>(R.id.palavra_passe_escrita).text.clear()
        findViewById<EditText>(R.id.idade_escrita).text.clear()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPasswordVisibilityToggle() {
        val editPalavraPasse = findViewById<EditText>(R.id.palavra_passe_escrita)

        editPalavraPasse.setOnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (editPalavraPasse.right - editPalavraPasse.compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {

                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {

                        editPalavraPasse.transformationMethod = null
                        editPalavraPasse.setCompoundDrawablesWithIntrinsicBounds(
                            null, null, ContextCompat.getDrawable(this, R.drawable.ic_eye), null
                        )
                    } else {

                        editPalavraPasse.transformationMethod = PasswordTransformationMethod.getInstance()
                        editPalavraPasse.setCompoundDrawablesWithIntrinsicBounds(
                            null, null, ContextCompat.getDrawable(this, R.drawable.ic_eye_closed), null
                        )
                    }
                    // Move o cursor para o final do texto
                    editPalavraPasse.setSelection(editPalavraPasse.text.length)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }
}