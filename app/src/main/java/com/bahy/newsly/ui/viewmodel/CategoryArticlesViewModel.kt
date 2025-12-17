package com.bahy.newsly.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahy.newsly.data.model.NewsArticle
import com.bahy.newsly.data.repository.AuthRepository
import com.bahy.newsly.data.repository.BookmarkRepository
import com.bahy.newsly.data.repository.NewsRepository
import com.bahy.newsly.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class CategoryArticlesUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val articles: List<NewsArticle> = emptyList(),
    val bookmarkedArticleIds: Set<String> = emptySet(),
    val hasMore: Boolean = true,
    val currentPage: Int = 1
)

class CategoryArticlesViewModel(
    private val newsRepository: NewsRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val authRepository: AuthRepository,
    private val category: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryArticlesUiState())
    val uiState: StateFlow<CategoryArticlesUiState> = _uiState.asStateFlow()

    private var userId: String = ""

    init {
        observeAuthState()
        loadInitialArticles()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                userId = user?.id ?: ""
                updateBookmarkedStatus()
            }
        }
    }

    private fun loadInitialArticles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, currentPage = 1)
            
            try {
                val articles = newsRepository.getCategoryArticles(category, 1)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    articles = articles,
                    hasMore = articles.isNotEmpty(),
                    currentPage = 1
                )
                updateBookmarkedStatus()
            } catch (e: Exception) {
                Log.e("CategoryArticlesViewModel", "Error loading articles: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load articles: ${e.message}"
                )
            }
        }
    }

    fun loadMoreArticles() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore || _uiState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            val nextPage = _uiState.value.currentPage + 1
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            
            try {
                val newArticles = newsRepository.getCategoryArticles(category, nextPage)
                
                if (newArticles.isEmpty()) {
                    // No more articles available
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        hasMore = false
                    )
                } else {
                    // Check if we got new articles (not duplicates)
                    val currentArticleIds = _uiState.value.articles.map { it.id }.toSet()
                    val uniqueNewArticles = newArticles.filter { it.id !in currentArticleIds }
                    
                    if (uniqueNewArticles.isEmpty()) {
                        // All articles are duplicates, no more new content
                        _uiState.value = _uiState.value.copy(
                            isLoadingMore = false,
                            hasMore = false
                        )
                    } else {
                        // Add new articles
                        _uiState.value = _uiState.value.copy(
                            isLoadingMore = false,
                            articles = _uiState.value.articles + uniqueNewArticles,
                            currentPage = nextPage,
                            hasMore = true
                        )
                        updateBookmarkedStatus()
                    }
                }
            } catch (e: Exception) {
                Log.e("CategoryArticlesViewModel", "Error loading more articles: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingMore = false,
                    error = "Failed to load more articles: ${e.message}"
                )
            }
        }
    }

    fun toggleBookmark(articleId: String) {
        viewModelScope.launch {
            if (userId.isEmpty()) {
                _uiState.value = _uiState.value.copy(error = "User not logged in to bookmark.")
                return@launch
            }
            
            val isBookmarked = bookmarkRepository.isBookmarked(userId, articleId)
            if (isBookmarked) {
                bookmarkRepository.removeBookmark(userId, articleId)
            } else {
                // Find the article in the current list
                val article = _uiState.value.articles.find { it.id == articleId }
                if (article != null) {
                    bookmarkRepository.addBookmark(userId, article)
                }
            }
            updateBookmarkedStatus()
        }
    }

    private fun updateBookmarkedStatus() {
        viewModelScope.launch {
            if (userId.isEmpty()) {
                _uiState.value = _uiState.value.copy(bookmarkedArticleIds = emptySet())
                return@launch
            }
            
            val articleIds = _uiState.value.articles.map { it.id }
            val bookmarkedIds = articleIds.filter { articleId ->
                bookmarkRepository.isBookmarked(userId, articleId)
            }.toSet()

            _uiState.value = _uiState.value.copy(bookmarkedArticleIds = bookmarkedIds)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

