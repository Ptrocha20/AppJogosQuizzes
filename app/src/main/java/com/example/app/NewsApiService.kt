package com.example.app

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

interface NewsApiService {
    @GET("top-headlines")
    fun getNews(
        @Query("token") apiKey: String,
        @Query("lang") language: String = "pt",
        @Query("q") query: String = "technology"
    ): Call<NewsResponse>
}
