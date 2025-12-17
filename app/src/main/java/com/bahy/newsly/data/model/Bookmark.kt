package com.bahy.newsly.data.model

data class Bookmark(
    val id: String,
    val articleId: String,
    val userId: String,
    val createdAt: Long,
    val article: NewsArticle? = null // Store full article data for Firestore
)

