package com.bahy.newsly.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahy.newsly.data.model.Category
import com.bahy.newsly.data.model.NewsArticle
import com.bahy.newsly.data.repository.AuthRepository
import com.bahy.newsly.data.repository.BookmarkRepository
import com.bahy.newsly.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val categories: List<Category> = emptyList(),
    val newsArticles: List<NewsArticle> = emptyList(),
    val recommendedArticles: List<NewsArticle> = emptyList(),
    val bookmarkedArticleIds: Set<String> = emptySet()
)

class HomeViewModel(
    private val newsRepository: NewsRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadNewsArticles()
        loadRecommendedArticles()
    }

    private fun loadCategories() {
        val categories = listOf(
            Category("random", "Random", "🎲", isSelected = true),
            Category("sports", "Sports", "⚽"),
            Category("gaming", "Gaming", "🎮"),
            Category("politics", "Politics", "⚖️")
        )
        _uiState.value = _uiState.value.copy(categories = categories)
    }

    fun selectCategory(categoryName: String) {
        val updatedCategories = _uiState.value.categories.map { category ->
            category.copy(isSelected = category.name == categoryName)
        }
        _uiState.value = _uiState.value.copy(
            categories = updatedCategories,
            selectedCategory = categoryName
        )
        loadNewsArticles(categoryName)
    }

    fun loadNewsArticles(category: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            newsRepository.getNewsArticles(category).collect { articles ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    newsArticles = articles
                )
                updateBookmarkedStatus()
            }
        }
    }

    private fun loadRecommendedArticles() {
        viewModelScope.launch {
            newsRepository.getRecommendedArticles().collect { articles ->
                _uiState.value = _uiState.value.copy(recommendedArticles = articles)
                updateBookmarkedStatus()
            }
        }
    }

    fun toggleBookmark(articleId: String) {
        viewModelScope.launch {
            val user = authRepository.currentUser.firstOrNull() ?: return@launch
            val userId = user.id
            val isBookmarked = bookmarkRepository.isBookmarked(userId, articleId)
            
            if (isBookmarked) {
                bookmarkRepository.removeBookmark(userId, articleId)
            } else {
                // Find the article in the current lists
                val article = (_uiState.value.newsArticles + _uiState.value.recommendedArticles)
                    .find { it.id == articleId }
                
                if (article != null) {
                    bookmarkRepository.addBookmark(userId, article)
                }
            }
            
            updateBookmarkedStatus()
        }
    }

    private fun updateBookmarkedStatus() {
        viewModelScope.launch {
            val user = authRepository.currentUser.firstOrNull() ?: return@launch
            val userId = user.id
            val allArticleIds = (_uiState.value.newsArticles + _uiState.value.recommendedArticles)
                .map { it.id }
                .toSet()
            
            val bookmarkedIds = allArticleIds.filter { articleId ->
                bookmarkRepository.isBookmarked(userId, articleId)
            }.toSet()
            
            _uiState.value = _uiState.value.copy(bookmarkedArticleIds = bookmarkedIds)
        }
    }

    fun searchArticles(query: String) {
        if (query.isEmpty()) {
            loadNewsArticles(_uiState.value.selectedCategory)
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            newsRepository.searchArticles(query).collect { articles ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    newsArticles = articles
                )
                updateBookmarkedStatus()
            }
        }
    }
}

