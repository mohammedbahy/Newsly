package com.bahy.newsly.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bahy.newsly.R
import com.bahy.newsly.data.model.toNewsArticle
import com.bahy.newsly.data.remote.RetrofitClient
import com.bahy.newsly.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first

class NewsNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        try {
            NotificationUtils.ensureChannels(applicationContext)

            val prefs = UserPreferencesRepository(applicationContext)
            val enabled = prefs.notificationsEnabled.first()
            if (!enabled) return Result.success()

            val apiKey = applicationContext.getString(R.string.gnews_api_key)
            if (apiKey.isBlank()) return Result.success()

            val country = prefs.country.first()

            val response = RetrofitClient.gNewsApiService.getTopHeadlines(
                category = "general",
                lang = "en",
                country = country,
                apiKey = apiKey
            )

            val latest = response.articles
                .map { it.toNewsArticle("General") }
                .firstOrNull { it.id.isNotBlank() && !it.url.isNullOrBlank() }
                ?: return Result.success()

            val lastNotifiedId = prefs.lastNotifiedArticleId.first()
            if (lastNotifiedId.isBlank()) {
                // First run: store but don't notify
                prefs.setLastNotifiedArticleId(latest.id)
                return Result.success()
            }

            if (latest.id == lastNotifiedId) return Result.success()

            // Open article URL in browser on tap
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(latest.url))
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(applicationContext, NotificationUtils.CHANNEL_ID_NEWS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(latest.title)
                .setContentText(latest.description.ifBlank { latest.source })
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        buildString {
                            append(latest.description)
                            if (latest.source.isNotBlank()) {
                                append("\n")
                                append(latest.source)
                            }
                        }
                    )
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(applicationContext).notify(1001, notification)
            prefs.setLastNotifiedArticleId(latest.id)

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}


