package com.bahy.newsly.data.repository

import android.content.Context
import android.util.Log
import com.bahy.newsly.R
import com.bahy.newsly.data.model.NewsArticle
import com.bahy.newsly.data.model.toNewsArticle
import com.bahy.newsly.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.util.concurrent.ConcurrentHashMap

class NewsRepository(private val context: Context) {
    
    private val apiService = RetrofitClient.gNewsApiService
    private val apiKey = context.getString(R.string.gnews_api_key)
    private val userPreferencesRepository = UserPreferencesRepository(context)

    // In-memory cache so any article the user can see is always openable by id
    private val articleCache = ConcurrentHashMap<String, NewsArticle>()

    private fun isOpenable(article: NewsArticle): Boolean {
        return article.id.isNotBlank() && !article.url.isNullOrBlank()
    }

    private fun cacheArticles(articles: List<NewsArticle>) {
        articles.forEach { article ->
            if (article.id.isNotBlank()) {
                articleCache[article.id] = article
            }
        }
    }
    
    init {
        // Validate API key on initialization
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") {
            Log.e("NewsRepository", "GNews API key is missing or not configured!")
        } else {
            Log.d("NewsRepository", "GNews API key loaded: ${apiKey.take(8)}...")
        }
    }
    
    // Category mapping from app categories to GNews categories
    private val categoryMapping = mapOf(
        "sports" to "sports",
        "politics" to "politics",
        "gaming" to "technology", // GNews doesn't have gaming, using technology
        "life" to "general",
        "animals" to "general",
        "nature" to "general",
        "food" to "general",
        "art" to "general",
        "history" to "general",
        "fashion" to "general",
        "covid-19" to "health",
        "middle east" to "world",
        "random" to "general",
        "general" to "general"
    )

    suspend fun getNewsArticles(category: String? = null): Flow<List<NewsArticle>> = flow {
        try {
            if (apiKey.isBlank()) {
                Log.e("NewsRepository", "API key is empty! Please configure GNews API key in strings.xml")
                emit(emptyList())
                return@flow
            }
            
            val gNewsCategory = category?.let { categoryMapping[it.lowercase()] } ?: "general"
            val country = userPreferencesRepository.country.first()
            
            Log.d("NewsRepository", "Fetching news articles - Category: $gNewsCategory, Country: $country")
            
            val response = apiService.getTopHeadlines(
                category = gNewsCategory,
                lang = "en",
                country = country,
                apiKey = apiKey
            )
            
            val articles = response.articles
                .map { it.toNewsArticle(category ?: "General") }
                .filter { isOpenable(it) } // show only openable articles
            cacheArticles(articles)
            emit(articles)
        } catch (e: retrofit2.HttpException) {
            when (e.code()) {
                403 -> {
                    Log.e("NewsRepository", "HTTP 403 Forbidden - API key may be invalid, expired, or rate limit exceeded. Please check your GNews API key.")
                }
                401 -> {
                    Log.e("NewsRepository", "HTTP 401 Unauthorized - Invalid API key.")
                }
                429 -> {
                    Log.e("NewsRepository", "HTTP 429 Too Many Requests - Rate limit exceeded. Please wait before making more requests.")
                }
                else -> {
                    Log.e("NewsRepository", "HTTP ${e.code()} Error fetching news articles: ${e.message()}")
                }
            }
            emit(emptyList())
        } catch (e: Exception) {
            Log.e("NewsRepository", "Error fetching news articles: ${e.message}", e)
            emit(emptyList())
        }
    }

    suspend fun getRecommendedArticles(): Flow<List<NewsArticle>> = flow {
        try {
            if (apiKey.isBlank()) {
                Log.e("NewsRepository", "API key is empty! Please configure GNews API key in strings.xml")
                emit(emptyList())
                return@flow
            }
            
            val country = userPreferencesRepository.country.first()
            val response = apiService.getTopHeadlines(
                category = "general",
                lang = "en",
                country = country,
                apiKey = apiKey
            )
            
            val articles = response.articles
                .map { it.toNewsArticle("General") }
                .filter { isOpenable(it) } // show only openable articles
                .take(3)
            cacheArticles(articles)
            emit(articles)
        } catch (e: retrofit2.HttpException) {
            when (e.code()) {
                403 -> {
                    Log.e("NewsRepository", "HTTP 403 Forbidden - API key may be invalid, expired, or rate limit exceeded. Please check your GNews API key.")
                }
                401 -> {
                    Log.e("NewsRepository", "HTTP 401 Unauthorized - Invalid API key.")
                }
                429 -> {
                    Log.e("NewsRepository", "HTTP 429 Too Many Requests - Rate limit exceeded. Please wait before making more requests.")
                }
                else -> {
                    Log.e("NewsRepository", "HTTP ${e.code()} Error fetching recommended articles: ${e.message()}")
                }
            }
            emit(emptyList())
        } catch (e: Exception) {
            Log.e("NewsRepository", "Error fetching recommended articles: ${e.message}", e)
            emit(emptyList())
        }
    }

    suspend fun searchArticles(query: String): Flow<List<NewsArticle>> = flow {
        try {
            if (query.isBlank()) {
                emit(emptyList())
                return@flow
            }
            
            if (apiKey.isBlank()) {
                Log.e("NewsRepository", "API key is empty! Please configure GNews API key in strings.xml")
                emit(emptyList())
                return@flow
            }
            
            val response = apiService.searchNews(
                query = query,
                lang = "en",
                apiKey = apiKey
            )
            
            val articles = response.articles
                .map { it.toNewsArticle("Search") }
                .filter { isOpenable(it) } // show only openable articles
            cacheArticles(articles)
            emit(articles)
        } catch (e: retrofit2.HttpException) {
            when (e.code()) {
                403 -> {
                    Log.e("NewsRepository", "HTTP 403 Forbidden - API key may be invalid, expired, or rate limit exceeded. Please check your GNews API key.")
                }
                401 -> {
                    Log.e("NewsRepository", "HTTP 401 Unauthorized - Invalid API key.")
                }
                429 -> {
                    Log.e("NewsRepository", "HTTP 429 Too Many Requests - Rate limit exceeded. Please wait before making more requests.")
                }
                else -> {
                    Log.e("NewsRepository", "HTTP ${e.code()} Error searching articles: ${e.message()}")
                }
            }
            emit(emptyList())
        } catch (e: Exception) {
            Log.e("NewsRepository", "Error searching articles: ${e.message}", e)
            emit(emptyList())
        }
    }

    suspend fun getCategoryArticles(category: String, page: Int = 1): List<NewsArticle> {
        try {
            if (apiKey.isBlank()) {
                Log.e("NewsRepository", "API key is empty! Please configure GNews API key in strings.xml")
                return emptyList()
            }
            
            val gNewsCategory = categoryMapping[category.lowercase()] ?: "general"
            val country = userPreferencesRepository.country.first()
            val response = apiService.getTopHeadlinesWithPage(
                category = gNewsCategory,
                lang = "en",
                country = country,
                page = page,
                apiKey = apiKey
            )
            
            val articles = response.articles
                .map { it.toNewsArticle(category) }
                .filter { isOpenable(it) } // show only openable articles
            cacheArticles(articles)
            return articles
        } catch (e: retrofit2.HttpException) {
            when (e.code()) {
                403 -> {
                    Log.e("NewsRepository", "HTTP 403 Forbidden - API key may be invalid, expired, or rate limit exceeded. Please check your GNews API key.")
                }
                401 -> {
                    Log.e("NewsRepository", "HTTP 401 Unauthorized - Invalid API key.")
                }
                429 -> {
                    Log.e("NewsRepository", "HTTP 429 Too Many Requests - Rate limit exceeded. Please wait before making more requests.")
                }
                else -> {
                    Log.e("NewsRepository", "HTTP ${e.code()} Error fetching category articles: ${e.message()}")
                }
            }
            throw e
        } catch (e: Exception) {
            Log.e("NewsRepository", "Error fetching category articles: ${e.message}", e)
            throw e
        }
    }

    suspend fun getArticleById(id: String): NewsArticle? {
        // GNews API doesn't have a direct endpoint for getting article by ID
        // We'll need to fetch all articles and find the one with matching ID
        // First try the in-memory cache (covers all items shown in lists)
        articleCache[id]?.let { cached ->
            if (isOpenable(cached)) return cached
        }
        return try {
            val country = userPreferencesRepository.country.first()
            val response = apiService.getTopHeadlines(
                category = "general",
                lang = "en",
                country = country,
                apiKey = apiKey
            )
            val found = response.articles.find { it.id == id }?.toNewsArticle()
            if (found != null) {
                articleCache[found.id] = found
            }
            found
        } catch (e: Exception) {
            Log.e("NewsRepository", "Error fetching article by ID: ${e.message}", e)
            null
        }
    }
}

