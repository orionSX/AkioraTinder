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
import org.json.JSONObject

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
                
                // Parse the result to check if it contains chat or test information
                try {
                    val jsonResponse = JSONObject(result)
                    
                    // Check if the response contains chat information
                    if (jsonResponse.has("_id") && jsonResponse.has("user_1")) {

                        val chatId = jsonResponse.optString("_id", "")
                        if (chatId.isNotEmpty()) {
                            _uiState.value = NavigateToChat(chatId)
                            removeProfile(formId) // Remove the profile after navigating to chat
                        } else {
                            _error.value = "Ошибка получения информации о чате"
                        }
                    }
                    else{
                        val profile = _profiles.value.find { it.id == formId }
                        _uiState.value = if (profile != null && profile.formTest != null && profile.formTest.questions.isNotEmpty()) {
                            NavigateToTest(formId = formId)
                        } else {
                            removeProfile(formId)
                            ShowRecommendations
                        }
                    }
                } catch (jsonException: Exception) {
                    // If JSON parsing fails, check if result contains chat-like information
                    if (result.contains("chat", ignoreCase = true) || 
                        result.contains("_id") || result.contains("user_1")) {
                        // Try to extract chat ID from text
                        val regex = "(_id|id|chat_id|chatId)[=:\"\\s]+([\\w-]+)".toRegex(RegexOption.IGNORE_CASE)
                        val matchResult = regex.find(result)
                        val chatId = matchResult?.groupValues?.getOrNull(2)
                        
                        if (!chatId.isNullOrEmpty()) {
                            _uiState.value = NavigateToChat(chatId)
                            removeProfile(formId)
                        } else {
                            // No chat found, check for test
                            val profile = _profiles.value.find { it.id == formId }
                            if (profile != null && profile.formTest != null && profile.formTest.questions.isNotEmpty()) {
                                _uiState.value = NavigateToTest(formId = formId)
                            } else {
                                removeProfile(formId)
                            }
                        }
                    } else {
                        // No chat found, check for test
                        val profile = _profiles.value.find { it.id == formId }
                        if (profile != null && profile.formTest != null && profile.formTest.questions.isNotEmpty()) {
                            _uiState.value = NavigateToTest(formId = formId)
                        } else {
                            removeProfile(formId)
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Ошибка при лайке анкеты: ${e.message}"
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

                // Parse the response to check if it contains chat information
                try {
                    val jsonResponse = JSONObject(response)
                    
                    // Check if the response contains chat information
                    if (jsonResponse.has("_id") && jsonResponse.has("user_1")) {
                        // This is a chat object, extract the chat ID
                        val chatId = jsonResponse.optString("_id", "")
                        if (chatId.isNotEmpty()) {
                            // Test passed successfully and chat created
                            removeProfile(formId) // Remove the profile after getting chat info
                            _uiState.value = ShowTestResult(
                                success = true,
                                message = response  // Return the full response
                            )
                        } else {
                            // No chat ID found in response
                            _uiState.value = ShowTestResult(
                                success = true,
                                message = response
                            )
                        }
                    } else {
                        // No chat object found, but test might still be successful
                        _uiState.value = ShowTestResult(
                            success = true,
                            message = response
                        )
                    }
                } catch (jsonException: Exception) {
                    // If JSON parsing fails, just return the response as is
                    _uiState.value = ShowTestResult(
                        success = true,
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