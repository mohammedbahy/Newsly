package com.bahy.newsly.data.remote

import com.bahy.newsly.data.remote.model.GNewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GNewsApiService {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("category") category: String = "general",
        @Query("lang") lang: String = "en",
        @Query("country") country: String = "us",
        @Query("apikey") apiKey: String
    ): GNewsResponse
    
    @GET("top-headlines")
    suspend fun getTopHeadlinesWithPage(
        @Query("category") category: String = "general",
        @Query("lang") lang: String = "en",
        @Query("country") country: String = "us",
        @Query("page") page: Int = 1,
        @Query("apikey") apiKey: String
    ): GNewsResponse
    
    @GET("search")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("lang") lang: String = "en",
        @Query("apikey") apiKey: String
    ): GNewsResponse
}

