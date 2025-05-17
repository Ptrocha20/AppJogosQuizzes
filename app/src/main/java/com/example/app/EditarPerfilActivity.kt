package com.example.app

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var profileImageView: CircleImageView
    private lateinit var editImageButton: ImageView
    private lateinit var editTextNome: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextDataNascimento: TextInputEditText
    private lateinit var buttonSalvar: Button
    private lateinit var buttonCancelar: Button

    private var selectedImageUri: Uri? = null
    private var isGoogleSignIn = false
    private var calendar = Calendar.getInstance()

    // Launcher para seleção de imagem da galeria
    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .into(profileImageView)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.perfil)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Referenciar elementos do layout
        profileImageView = findViewById(R.id.profileImageView)
        editImageButton = findViewById(R.id.editImageButton)
        editTextNome = findViewById(R.id.editTextNome)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextDataNascimento = findViewById(R.id.editTextDataNascimento)
        buttonSalvar = findViewById(R.id.buttonSalvar)
        buttonCancelar = findViewById(R.id.buttonCancelar)

        // Verificar se o usuário está logado
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não está logado", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Verificar se o login foi feito com Google
        for (userInfo in currentUser.providerData) {
            if (userInfo.providerId == GoogleAuthProvider.PROVIDER_ID) {
                isGoogleSignIn = true
                break
            }
        }

        // Carregar dados do usuário
        carregarDadosUsuario()

        // Configurar listeners
        setupListeners()
    }

    private fun carregarDadosUsuario() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("Utilizadores").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Preencher os campos com os dados do Firestore
                        editTextNome.setText(document.getString("nome"))
                        editTextEmail.setText(document.getString("email"))

                        // Carregar data de nascimento diretamente do Firestore
                        val dataNascimento = document.getString("dataNascimento")
                        if (!dataNascimento.isNullOrEmpty()) {
                            editTextDataNascimento.setText(dataNascimento)

                            // Atualizar o Calendar com a data recuperada
                            try {
                                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val date = sdf.parse(dataNascimento)
                                if (date != null) {
                                    calendar.time = date
                                }
                            } catch (e: Exception) {
                                // Se houver erro ao parsear a data, ignorar
                            }
                        }

                        // Carregar foto de perfil se existir
                        val profileImageUrl = document.getString("profileImageUrl")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(profileImageUrl)
                                .centerCrop()
                                .into(profileImageView)
                        }

                        // Desabilitar edição de email para usuários que fizeram login por email
                        if (!isGoogleSignIn) {
                            editTextEmail.isEnabled = false
                            editTextEmail.alpha = 0.5f
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao carregar dados: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupListeners() {
        // Seleção de imagem
        editImageButton.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getContent.launch(galleryIntent)
        }

        // Seleção de data de nascimento
        editTextDataNascimento.setOnClickListener {
            showDatePickerDialog()
        }

        // Botão salvar
        buttonSalvar.setOnClickListener {
            salvarAlteracoes()
        }

        // Botão cancelar
        buttonCancelar.setOnClickListener {
            finish()
        }
    }

    private fun showDatePickerDialog() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = Date().time  // Não permitir datas futuras
            show()
        }
    }

    private fun updateDateInView() {
        val format = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        editTextDataNascimento.setText(sdf.format(calendar.time))
    }

    private fun calcularIdade(dataNascimento: Calendar): Int {
        val hoje = Calendar.getInstance()
        var idade = hoje.get(Calendar.YEAR) - dataNascimento.get(Calendar.YEAR)

        if (hoje.get(Calendar.DAY_OF_YEAR) < dataNascimento.get(Calendar.DAY_OF_YEAR)) {
            idade--
        }

        return idade
    }

    private fun salvarAlteracoes() {
        val nome = editTextNome.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val dataNascimentoStr = editTextDataNascimento.text.toString().trim()

        // Validação básica
        if (nome.isEmpty()) {
            Toast.makeText(this, "Por favor, insira seu nome", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor, insira um email válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (dataNascimentoStr.isEmpty()) {
            Toast.makeText(this, "Por favor, insira sua data de nascimento", Toast.LENGTH_SHORT).show()
            return
        }

        // Calcular idade a partir da data de nascimento
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dataNascimento = Calendar.getInstance()
        try {
            dataNascimento.time = sdf.parse(dataNascimentoStr) ?: Date()
        } catch (e: Exception) {
            Toast.makeText(this, "Formato de data inválido. Use dd/MM/yyyy", Toast.LENGTH_SHORT).show()
            return
        }

        val idade = calcularIdade(dataNascimento)
        if (idade <= 0) {
            Toast.makeText(this, "Data de nascimento inválida", Toast.LENGTH_SHORT).show()
            return
        }

        // Iniciar o processo de atualização
        atualizarPerfilUsuario(nome, email, idade, dataNascimentoStr)
    }

    private fun atualizarPerfilUsuario(nome: String, email: String, idade: Int, dataNascimento: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Erro: Usuário não encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        // Primeiro, enviar a imagem se uma nova foi selecionada
        if (selectedImageUri != null) {
            val reference = storage.reference.child("profile_images").child(userId)
            val uploadTask = reference.putFile(selectedImageUri!!)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                reference.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val imageUrl = task.result.toString()
                    atualizarDadosNoFirestore(userId, nome, email, idade, dataNascimento, imageUrl)
                } else {
                    Toast.makeText(this, "Erro ao enviar imagem: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    // Continuar atualizando outros dados mesmo se a imagem falhar
                    atualizarDadosNoFirestore(userId, nome, email, idade, dataNascimento, null)
                }
            }
        } else {
            // Sem nova imagem, apenas atualizar outros dados
            atualizarDadosNoFirestore(userId, nome, email, idade, dataNascimento, null)
        }
    }

    private fun atualizarDadosNoFirestore(
        userId: String,
        nome: String,
        email: String,
        idade: Int,
        dataNascimento: String,
        imageUrl: String?
    ) {
        // Recuperar dados existentes primeiro para não perder informações
        db.collection("Utilizadores").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Manter os dados existentes
                    val xpAtual = document.getLong("xp") ?: 0L
                    val nivelAtual = document.getLong("nivel")?.toInt() ?: 1
                    val quizzesConcluidos = document.getLong("quizzesConcluidos")?.toInt() ?: 0
                    val jogosConcluidos = document.getLong("jogosConcluidos")?.toInt() ?: 0

                    @Suppress("UNCHECKED_CAST")
                    val conquistasObtidas = document.get("conquistasObtidas") as? List<String> ?: listOf()

                    // Obter URL de imagem existente, se não houver uma nova
                    val profileImageUrl = if (imageUrl != null) {
                        imageUrl
                    } else {
                        document.getString("profileImageUrl") ?: ""
                    }

                    // Criar mapa com todos os dados atualizados
                    val dadosAtualizados = hashMapOf(
                        "nome" to nome,
                        "email" to email,
                        "idade" to idade,
                        "dataNascimento" to dataNascimento,  // Armazenar data de nascimento completa
                        "xp" to xpAtual,
                        "nivel" to nivelAtual,
                        "quizzesConcluidos" to quizzesConcluidos,
                        "jogosConcluidos" to jogosConcluidos,
                        "conquistasObtidas" to conquistasObtidas,
                        "profileImageUrl" to profileImageUrl
                    )

                    // Atualizar no Firestore
                    db.collection("Utilizadores").document(userId)
                        .set(dadosAtualizados)
                        .addOnSuccessListener {

                            if (isGoogleSignIn && auth.currentUser?.email != email) {
                                // Para Google, não podemos mudar o email diretamente, só no Firestore
                                Toast.makeText(this, "Perfil atualizado! Note que para alterar seu email principal no Google, você deve fazer isso nas configurações da sua conta Google.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                            }

                            // Voltar para a atividade anterior
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Erro ao atualizar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao recuperar dados: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}