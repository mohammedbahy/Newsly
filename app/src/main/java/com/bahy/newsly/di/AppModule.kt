package com.bahy.newsly.di

import android.content.Context
import com.bahy.newsly.data.repository.AuthRepository
import com.bahy.newsly.data.repository.BookmarkRepository
import com.bahy.newsly.data.repository.NewsRepository
import com.bahy.newsly.data.repository.UserPreferencesRepository

object AppModule {
    private var authRepository: AuthRepository? = null
    private var newsRepository: NewsRepository? = null
    private var bookmarkRepository: BookmarkRepository? = null
    private var userPreferencesRepository: UserPreferencesRepository? = null

    fun provideAuthRepository(): AuthRepository {
        if (authRepository == null) {
            authRepository = AuthRepository()
        }
        return authRepository!!
    }

    fun provideNewsRepository(context: Context): NewsRepository {
        if (newsRepository == null) {
            newsRepository = NewsRepository(context)
        }
        return newsRepository!!
    }

    fun provideBookmarkRepository(newsRepository: NewsRepository? = null): BookmarkRepository {
        if (bookmarkRepository == null) {
            bookmarkRepository = BookmarkRepository()
        }
        return bookmarkRepository!!
    }

    fun provideUserPreferencesRepository(context: Context): UserPreferencesRepository {
        if (userPreferencesRepository == null) {
            userPreferencesRepository = UserPreferencesRepository(context)
        }
        return userPreferencesRepository!!
    }
}

