package com.bahy.newsly.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {
    companion object {
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val LANGUAGE = stringPreferencesKey("language")
        private val COUNTRY = stringPreferencesKey("country")
        private val LAST_NOTIFIED_ARTICLE_ID = stringPreferencesKey("last_notified_article_id")
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: false
    }

    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE] ?: "en"
    }

    val country: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[COUNTRY] ?: "us"
    }

    val lastNotifiedArticleId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LAST_NOTIFIED_ARTICLE_ID] ?: ""
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = language
        }
    }

    suspend fun setCountry(country: String) {
        context.dataStore.edit { preferences ->
            preferences[COUNTRY] = country
        }
    }

    suspend fun setLastNotifiedArticleId(articleId: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_NOTIFIED_ARTICLE_ID] = articleId
        }
    }
}

