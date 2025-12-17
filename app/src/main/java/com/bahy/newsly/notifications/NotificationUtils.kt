package com.bahy.newsly.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationUtils {
    const val CHANNEL_ID_NEWS = "news_updates"
    private const val CHANNEL_NAME_NEWS = "News updates"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(CHANNEL_ID_NEWS)
        if (existing != null) return

        val channel = NotificationChannel(
            CHANNEL_ID_NEWS,
            CHANNEL_NAME_NEWS,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications about new news articles"
        }

        manager.createNotificationChannel(channel)
    }
}


