package com.bahy.newsly.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.bahy.newsly.data.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CategoriesUiState(
    val categories: List<Category> = emptyList()
)

class CategoriesViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        val categories = listOf(
            Category("1", "Sports", "⚽"),
            Category("2", "Politics", "⚖️"),
            Category("3", "Life", "😊"),
            Category("4", "Gaming", "🎮"),
            Category("5", "Animals", "🐻"),
            Category("6", "Nature", "🌴"),
            Category("7", "Food", "🍔"),
            Category("8", "Art", "🎨"),
            Category("9", "History", "📜"),
            Category("10", "Fashion", "👗"),
            Category("11", "Covid-19", "😷"),
            Category("12", "Middle East", "⚔️")
        )
        _uiState.value = CategoriesUiState(categories = categories)
    }
}

