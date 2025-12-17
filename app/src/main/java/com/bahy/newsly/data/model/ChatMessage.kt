package com.bahy.newsly.data.model

data class ChatMessage(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val sender: Sender = Sender.USER,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class Sender { USER, BOT }
}

