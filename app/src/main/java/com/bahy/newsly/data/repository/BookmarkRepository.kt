package com.bahy.newsly.data.repository

import com.bahy.newsly.data.model.Bookmark
import com.bahy.newsly.data.model.NewsArticle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class BookmarkRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    
    private fun getBookmarksCollection(userId: String) = 
        firestore.collection("users").document(userId).collection("bookmarks")
    
    suspend fun addBookmark(userId: String, article: NewsArticle): Result<Bookmark> {
        return try {
            // Check if already bookmarked
            val existingBookmark = getBookmarksCollection(userId)
                .whereEqualTo("articleId", article.id)
                .limit(1)
                .get()
                .await()
            
            if (!existingBookmark.isEmpty) {
                return Result.failure(Exception("Article already bookmarked"))
            }
            
            val bookmarkId = System.currentTimeMillis().toString()
            val bookmark = Bookmark(
                id = bookmarkId,
                articleId = article.id,
                userId = userId,
                createdAt = System.currentTimeMillis(),
                article = article
            )
            
            // Save to Firestore
            getBookmarksCollection(userId)
                .document(bookmarkId)
                .set(bookmarkToMap(bookmark))
                .await()
            
            Result.success(bookmark)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun removeBookmark(userId: String, articleId: String): Result<Unit> {
        return try {
            val querySnapshot = getBookmarksCollection(userId)
                .whereEqualTo("articleId", articleId)
                .get()
                .await()
            
            val batch = firestore.batch()
            querySnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun isBookmarked(userId: String, articleId: String): Boolean {
        return try {
            val querySnapshot = getBookmarksCollection(userId)
                .whereEqualTo("articleId", articleId)
                .limit(1)
                .get()
                .await()
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
    
    fun getBookmarkedArticles(userId: String): Flow<List<NewsArticle>> {
        return callbackFlow {
            val listenerRegistration = getBookmarksCollection(userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        val articles = snapshot.documents.mapNotNull { doc ->
                            mapToBookmark(doc.data ?: emptyMap())?.article
                        }
                        trySend(articles)
                    } else {
                        trySend(emptyList())
                    }
                }
            
            awaitClose { listenerRegistration.remove() }
        }
    }
    
    // Helper functions to convert between Bookmark and Map for Firestore
    private fun bookmarkToMap(bookmark: Bookmark): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to bookmark.id,
            "articleId" to bookmark.articleId,
            "userId" to bookmark.userId,
            "createdAt" to bookmark.createdAt
        )
        
        bookmark.article?.let { article ->
            map["article"] = articleToMap(article)
        }
        
        return map
    }
    
    private fun mapToBookmark(map: Map<String, Any>): Bookmark? {
        return try {
            val articleMap = map["article"] as? Map<String, Any>
            val article = articleMap?.let { mapToArticle(it) }
            
            Bookmark(
                id = map["id"] as? String ?: "",
                articleId = map["articleId"] as? String ?: "",
                userId = map["userId"] as? String ?: "",
                createdAt = (map["createdAt"] as? Long) ?: (map["createdAt"] as? Number)?.toLong() ?: 0L,
                article = article
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun articleToMap(article: NewsArticle): Map<String, Any> {
        return mapOf(
            "id" to article.id,
            "title" to article.title,
            "description" to article.description,
            "content" to (article.content ?: ""),
            "category" to article.category,
            "imageUrl" to (article.imageUrl ?: ""),
            "author" to article.author,
            "publishedAt" to article.publishedAt,
            "source" to article.source,
            "url" to (article.url ?: "")
        )
    }
    
    private fun mapToArticle(map: Map<String, Any>): NewsArticle {
        return NewsArticle(
            id = map["id"] as? String ?: "",
            title = map["title"] as? String ?: "",
            description = map["description"] as? String ?: "",
            content = map["content"] as? String,
            category = map["category"] as? String ?: "General",
            imageUrl = map["imageUrl"] as? String,
            author = map["author"] as? String ?: "",
            publishedAt = (map["publishedAt"] as? Long) ?: (map["publishedAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            source = map["source"] as? String ?: "",
            url = map["url"] as? String
        )
    }
}
