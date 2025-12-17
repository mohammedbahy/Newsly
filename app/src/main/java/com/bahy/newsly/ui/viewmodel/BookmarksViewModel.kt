package com.bahy.newsly.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahy.newsly.data.model.NewsArticle
import com.bahy.newsly.data.repository.AuthRepository
import com.bahy.newsly.data.repository.BookmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class BookmarksUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val bookmarkedArticles: List<NewsArticle> = emptyList()
)

class BookmarksViewModel(
    private val bookmarkRepository: BookmarkRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    init {
        loadBookmarks()
    }

    private fun loadBookmarks() {
        viewModelScope.launch {
            val user = authRepository.currentUser.firstOrNull() ?: return@launch
            val userId = user.id
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            bookmarkRepository.getBookmarkedArticles(userId).collect { articles ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    bookmarkedArticles = articles
                )
            }
        }
    }

    fun removeBookmark(articleId: String) {
        viewModelScope.launch {
            val user = authRepository.currentUser.firstOrNull() ?: return@launch
            val userId = user.id
            bookmarkRepository.removeBookmark(userId, articleId)
                .onSuccess {
                    loadBookmarks()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message
                    )
                }
        }
    }
}

