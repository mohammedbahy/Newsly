package com.bahy.newsly.data.remote.model

import com.google.gson.annotations.SerializedName

data class GNewsResponse(
    @SerializedName("totalArticles")
    val totalArticles: Int,
    @SerializedName("articles")
    val articles: List<GNewsArticle>
)

data class GNewsArticle(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("content")
    val content: String?,
    @SerializedName("url")
    val url: String,
    @SerializedName("image")
    val image: String?,
    @SerializedName("publishedAt")
    val publishedAt: String,
    @SerializedName("lang")
    val lang: String,
    @SerializedName("source")
    val source: GNewsSource
)

data class GNewsSource(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String,
    @SerializedName("url")
    val url: String?
)

