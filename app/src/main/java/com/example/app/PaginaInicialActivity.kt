package com.example.app

import NewsAdapter
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class PaginaInicialActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.pagina_inicial)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewNoticias)
        val adapter = NewsAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val apiKey = "dccdf2053b401a5654c8844bab5dec5d"

        RetrofitClient.instance.getNews(apiKey).enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful) {
                    val noticias = response.body()?.articles ?: emptyList()
                    recyclerView.adapter = NewsAdapter(noticias)
                } else {
                    Log.e("NEWS", "Erro: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                Log.e("NEWS", "Falha na requisição: ${t.message}")
            }
        })

        BottomNavigation(this).setupBottomNavigation()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_quizzes -> {
                    startActivity(Intent(this, JogosActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ContaActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, DefinicoesActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }


}
