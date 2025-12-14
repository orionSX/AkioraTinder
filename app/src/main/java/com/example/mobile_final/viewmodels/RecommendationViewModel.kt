package com.example.mobile_final.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_final.dto.Answer
import com.example.mobile_final.dto.PlayerProfile
import com.example.mobile_final.services.ApiService
import com.example.mobile_final.viewmodels.RecommendationViewModel.UiState.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecommendationViewModel(private val apiService: ApiService) : ViewModel() {

    private val _profiles = MutableStateFlow<List<PlayerProfile>>(emptyList())
    val profiles: StateFlow<List<PlayerProfile>> = _profiles

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _uiState = MutableStateFlow<UiState>(IDLE)
    val uiState: StateFlow<UiState> = _uiState

    init {
        loadRecommendedProfiles()
    }

    fun loadRecommendedProfiles() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _uiState.value = IDLE

            try {
                val userId = apiService.getCurrentUserId()
                val recommendedProfiles = apiService.getRecommendedForms(userId)
                _profiles.value = recommendedProfiles
                _uiState.value = ShowRecommendations
            } catch (e: Exception) {
                _error.value = e.message
                _uiState.value = ShowRecommendations // Все равно показываем рекомендации (пустой список)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun likeProfile(formId: String) {
        viewModelScope.launch {
            try {
                val result = apiService.likeForm(formId)
                if (!result) {
                    _error.value = "Ошибка при лайке анкеты"
                } else {
                    // Remove the liked profile from the list
                    removeProfile(formId)
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun dislikeProfile(formId: String) {
        viewModelScope.launch {
            try {
                val result = apiService.dislikeForm(formId)
                if (!result) {
                    _error.value = "Ошибка при дизлайке анкеты"
                } else {
                    // Remove the disliked profile from the list
                    removeProfile(formId)
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun passTest(formId: String, answers: List<Answer>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = apiService.passTest(formId, answers)

                if (response.contains("success", ignoreCase = true) ||
                    response.contains("успешно", ignoreCase = true)) {
                    // Тест пройден успешно, удаляем анкету
                    removeProfile(formId)
                    _uiState.value = ShowTestResult(
                        success = true,
                        message = "Тест пройден успешно!"
                    )
                } else {
                    // Ошибка при прохождении теста
                    _uiState.value = ShowTestResult(
                        success = false,
                        message = response
                    )
                }
            } catch (e: Exception) {
                _error.value = "Ошибка при отправке теста: ${e.message}"
                _uiState.value = ShowTestResult(
                    success = false,
                    message = e.message
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun removeProfile(formId: String) {
        _profiles.value = _profiles.value.filter { it.id != formId }
    }

    fun navigateToTest(formId: String) {
        _uiState.value = NavigateToTest(formId = formId)
    }

    fun navigateToChat(chatId: String) {
        _uiState.value = NavigateToChat(chatId)
    }

    fun resetNavigationState() {
        _uiState.value = IDLE
    }

    fun navigateBackToRecommendations() {
        // Просто сбрасываем состояние, не загружаем заново
        _uiState.value = ShowRecommendations
    }

    fun resetToIdle() {
        _uiState.value = IDLE
    }

    sealed class UiState {
        object IDLE : UiState()
        object ShowRecommendations : UiState()
        data class NavigateToTest(val formId: String) : UiState()
        data class NavigateToChat(val chatId: String) : UiState()
        data class ShowTestResult(val success: Boolean, val message: String? = null) : UiState()
    }
}