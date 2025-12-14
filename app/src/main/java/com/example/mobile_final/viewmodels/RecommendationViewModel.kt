package com.example.mobile_final.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_final.dto.Answer
import com.example.mobile_final.dto.PlayerProfile
import com.example.mobile_final.services.ApiService
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

    private val _uiState = MutableStateFlow(UiState.IDLE)
    val uiState: StateFlow<UiState> = _uiState

    init {
        loadRecommendedProfiles()
    }

    fun loadRecommendedProfiles() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val recommendedProfiles = apiService.getRecommendedForms()
                _profiles.value = recommendedProfiles
            } catch (e: Exception) {
                _error.value = e.message
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

    fun passTest(formId: String, answers: List<Answer>): String {
        var result = ""
        viewModelScope.launch {
            try {
                val response = apiService.passTest(formId, answers)
                result = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }.join() // Wait for completion
        
        return result
    }

    private fun removeProfile(formId: String) {
        _profiles.value = _profiles.value.filter { it.id != formId }
    }

    fun navigateToTest(formId: String) {
        _uiState.value = UiState.NavigateToTest(formId)
    }

    fun navigateToChat(chatId: String) {
        _uiState.value = UiState.NavigateToChat(chatId)
    }

    fun resetNavigationState() {
        _uiState.value = UiState.IDLE
    }

    fun navigateBackToRecommendations() {
        loadRecommendedProfiles()
        _uiState.value = UiState.ShowRecommendations
    }

    sealed class UiState {
        object IDLE : UiState()
        object ShowRecommendations : UiState()
        data class NavigateToTest(val formId: String) : UiState()
        data class NavigateToChat(val chatId: String) : UiState()
        data class ShowTestResult(val success: Boolean, val message: String? = null) : UiState()
    }
}