package com.example.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.appbar.MaterialToolbar

class SobreActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var versionText: TextView
    private lateinit var termosUsoContainer: LinearLayout
    private lateinit var politicaPrivacidadeContainer: LinearLayout
    private lateinit var contatoContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sobre)

        // Inicializar as views
        initViews()

        // Configurar os listeners
        setupListeners()

        // Mostrar a versão atual do aplicativo
        setupVersionInfo()
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        versionText = findViewById(R.id.version_text)
        termosUsoContainer = findViewById(R.id.termos_uso_container)
        politicaPrivacidadeContainer = findViewById(R.id.politica_privacidade_container)
        contatoContainer = findViewById(R.id.contato_container)
    }

    private fun setupListeners() {
        // Botão voltar
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Termos de Uso
        termosUsoContainer.setOnClickListener {
            openWebPage("https://www.example.com/termos-de-uso")
        }

        // Política de Privacidade
        politicaPrivacidadeContainer.setOnClickListener {
            openWebPage("https://www.example.com/politica-de-privacidade")
        }

        // Contato
        contatoContainer.setOnClickListener {
            openEmailApp()
        }
    }

    private fun setupVersionInfo() {
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName
            versionText.text = "Versão $versionName"
        } catch (e: Exception) {
            versionText.text = "Versão 1.0.0"
        }
    }

    private fun openWebPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun openEmailApp() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:contato@segurancadigital.com")
            putExtra(Intent.EXTRA_SUBJECT, "Contato - App Segurança Digital")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}