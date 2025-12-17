package com.bahy.newsly.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bahy.newsly.data.model.User
import com.bahy.newsly.data.repository.AuthRepository
import com.bahy.newsly.data.repository.UserPreferencesRepository
import com.bahy.newsly.notifications.NewsNotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val notificationsEnabled: Boolean = false,
    val language: String = "en",
    val country: String = "us",
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeUser()
        observePreferences()
    }

    private fun observeUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(user = user)
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferencesRepository.notificationsEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
            }
        }
        
        viewModelScope.launch {
            userPreferencesRepository.language.collect { lang ->
                _uiState.value = _uiState.value.copy(language = lang)
            }
        }
        
        viewModelScope.launch {
            userPreferencesRepository.country.collect { country ->
                _uiState.value = _uiState.value.copy(country = country)
            }
        }
    }

    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setNotificationsEnabled(enabled)
            if (enabled) {
                NewsNotificationScheduler.schedule(context.applicationContext)
            } else {
                NewsNotificationScheduler.cancel(context.applicationContext)
            }
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            userPreferencesRepository.setLanguage(language)
        }
    }

    fun setCountry(country: String) {
        viewModelScope.launch {
            userPreferencesRepository.setCountry(country)
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            authRepository.signOut(context)
        }
    }
}

