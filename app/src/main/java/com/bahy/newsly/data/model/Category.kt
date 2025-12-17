package com.bahy.newsly.data.model

data class Category(
    val id: String,
    val name: String,
    val emoji: String,
    val isSelected: Boolean = false
)

