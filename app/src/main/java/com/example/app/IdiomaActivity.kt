package com.example.app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class IdiomaActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var aplicarButton: Button
    private lateinit var idiomasRadioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.idioma)

        backButton = findViewById(R.id.backButton)
        aplicarButton = findViewById(R.id.btn_aplicar)
        idiomasRadioGroup = findViewById(R.id.idiomas_radio_group)

        // Voltar à activity anterior
        backButton.setOnClickListener {
            finish()
        }

        aplicarButton.setOnClickListener {
            val selectedRadioId = idiomasRadioGroup.checkedRadioButtonId
            if (selectedRadioId != -1) {
                val radioButton = findViewById<RadioButton>(selectedRadioId)
                val idiomaSelecionado = radioButton.text.toString()

                // Aqui podes guardar o idioma ou aplicar lógica adicional
                Toast.makeText(this, "Idioma selecionado: $idiomaSelecionado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, selecione um idioma.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
