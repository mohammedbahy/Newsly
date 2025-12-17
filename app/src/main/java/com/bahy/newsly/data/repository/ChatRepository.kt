package com.bahy.newsly.data.repository

import com.bahy.newsly.data.model.ChatMessage
import com.bahy.newsly.data.remote.GeminiApiService
import com.bahy.newsly.data.remote.GeminiRetrofitClient
import com.bahy.newsly.data.remote.model.gemini.GenerateContentRequest
import com.bahy.newsly.data.remote.model.gemini.GeminiContent
import com.bahy.newsly.data.remote.model.gemini.GeminiPart
import com.bahy.newsly.data.remote.model.gemini.GeminiSystemInstruction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val geminiApi: GeminiApiService = GeminiRetrofitClient.api,
    private val geminiApiKey: String
) {

    private fun messagesCollection(userId: String) =
        firestore.collection("users").document(userId).collection("chat_messages")

    fun streamMessages(userId: String): Flow<List<ChatMessage>> {
        return callbackFlow {
            val listener = messagesCollection(userId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val messages = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(ChatMessage::class.java)
                    } ?: emptyList()
                    trySend(messages)
                }
            awaitClose { listener.remove() }
        }
    }

    suspend fun sendMessage(userId: String, text: String): Result<Unit> {
        if (geminiApiKey.isBlank()) {
            return Result.failure(IllegalStateException("Gemini API key is not configured"))
        }

        // Save user message first
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            userId = userId,
            text = text,
            sender = ChatMessage.Sender.USER,
            timestamp = System.currentTimeMillis()
        )
        messagesCollection(userId).document(userMessage.id).set(userMessage).await()

        // Call Gemini with system instruction for news-only chatbot
        return try {
            val systemInstruction = GeminiSystemInstruction(
                parts = listOf(
                    GeminiPart(
                        text = """You are a helpful news assistant chatbot specialized in news and current events. 
Your role is to:
- Answer questions about news, current events, and news-related topics
- Help users understand news articles and headlines
- Provide context about news stories
- Discuss news categories (sports, politics, technology, etc.)

IMPORTANT RULES:
- ONLY answer questions related to news, current events, and news-related topics
- If asked about topics unrelated to news (like cooking, math, general knowledge, personal advice, etc.), politely decline and redirect to news topics
- Say: "I'm a news assistant. I can help you with news and current events. Could you ask me something about news instead?"
- Keep responses concise and focused on news-related information
- Be friendly and professional"""
                    )
                )
            )
            
            val request = GenerateContentRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = text))
                    )
                ),
                systemInstruction = systemInstruction
            )

            val response = geminiApi.generateContent(geminiApiKey, request)
            val botText = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.trim()
                ?: "I couldn't generate a response."

            val botMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                userId = userId,
                text = botText,
                sender = ChatMessage.Sender.BOT,
                timestamp = System.currentTimeMillis()
            )
            messagesCollection(userId).document(botMessage.id).set(botMessage).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearConversation(userId: String): Result<Unit> {
        return try {
            val snapshot = messagesCollection(userId).get().await()
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

