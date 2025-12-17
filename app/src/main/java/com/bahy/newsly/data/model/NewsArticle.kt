package com.bahy.newsly.data.model

data class NewsArticle(
    val id: String,
    val title: String,
    val description: String,
    val content: String? = null,
    val category: String = "General",
    val imageUrl: String? = null,
    val author: String = "",
    val publishedAt: Long = System.currentTimeMillis(),
    val source: String = "",
    val url: String? = null,
    val isBookmarked: Boolean = false
)

// Extension function to convert GNews article to NewsArticle
fun com.bahy.newsly.data.remote.model.GNewsArticle.toNewsArticle(category: String = "General"): NewsArticle {
    return NewsArticle(
        id = this.id,
        title = this.title,
        description = this.description,
        content = this.content,
        category = category,
        imageUrl = this.image,
        author = "", // GNews API doesn't provide author in the response
        publishedAt = parsePublishedAt(this.publishedAt),
        source = this.source.name,
        url = this.url,
        isBookmarked = false
    )
}

private fun parsePublishedAt(publishedAt: String): Long {
    return try {
        // Format: "2025-12-12T12:21:12Z"
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
        dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        dateFormat.parse(publishedAt)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

