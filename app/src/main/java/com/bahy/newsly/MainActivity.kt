package com.bahy.newsly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bahy.newsly.ui.splash.NewslyApp
import com.bahy.newsly.notifications.NotificationUtils
import com.bahy.newsly.notifications.NewsNotificationScheduler
import com.bahy.newsly.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        NotificationUtils.ensureChannels(this)
        // Ensure scheduled work matches current preference (covers app restarts)
        CoroutineScope(Dispatchers.Default).launch {
            val prefs = UserPreferencesRepository(applicationContext)
            val enabled = prefs.notificationsEnabled.first()
            if (enabled) {
                NewsNotificationScheduler.schedule(applicationContext)
            }
        }
        setContent {
            NewslyApp()
        }
    }
}