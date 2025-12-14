package com.example.mobile_final.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobile_final.services.ApiService
import com.example.mobile_final.viewmodels.RecommendationViewModel
import org.json.JSONObject

@Composable
fun MainRecommendationScreen(
    apiService: ApiService,
    onNavigateToChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: RecommendationViewModel = viewModel {
        RecommendationViewModel(apiService)
    }

    val profiles by viewModel.profiles.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState) {
        when (uiState) {
            is RecommendationViewModel.UiState.NavigateToChat -> {
                val chatId = (uiState as RecommendationViewModel.UiState.NavigateToChat).chatId
                onNavigateToChat(chatId)
                viewModel.resetNavigationState()
            }
            is RecommendationViewModel.UiState.ShowRecommendations -> {
                viewModel.loadRecommendedProfiles()
                viewModel.resetNavigationState()
            }
            else -> {}
        }
    }

    when (val currentState = uiState) {
        is RecommendationViewModel.UiState.NavigateToTest -> {
            val formId = currentState.formId
            val profile = profiles.find { it.id == formId }
            
            if (profile != null && profile.formTest != null) {
                TestScreen(
                    questions = profile.formTest.questions,
                    onSubmit = { answers ->
                        // Submit test and handle response
                        val response = viewModel.passTest(formId, answers)
                        
                        try {
                            val jsonResponse = JSONObject(response)
                            
                            // Check if response contains a chat (has _id field indicating it's a chat object)
                            if (jsonResponse.has("_id") && jsonResponse.has("user_1")) {
                                // It's a chat object, extract the chat ID
                                val chatId = jsonResponse.optString("_id", "")
                                if (chatId.isNotEmpty()) {
                                    viewModel.navigateToChat(chatId)
                                } else {
                                    // If there's no chat ID, it might be a test result
                                    viewModel.navigateBackToRecommendations()
                                }
                            } else {
                                // Check if it's a test result with passed/failed status
                                val passed = jsonResponse.optBoolean("passed", false)
                                if (!passed) {
                                    // Test failed, go back to recommendations
                                    viewModel.navigateBackToRecommendations()
                                } else {
                                    // If it's a positive result, check if there's a chat
                                    val chatId = jsonResponse.optString("chat_id", "")
                                    if (chatId.isNotEmpty()) {
                                        viewModel.navigateToChat(chatId)
                                    } else {
                                        viewModel.navigateBackToRecommendations()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // If JSON parsing fails, assume test failed
                            viewModel.navigateBackToRecommendations()
                        }
                    },
                    onBack = {
                        viewModel.resetNavigationState()
                    }
                )
            } else {
                // If profile or test is not found, go back to recommendations
                viewModel.navigateBackToRecommendations()
            }
        }
        else -> {
            RecommendationScreen(
                profiles = profiles,
                onLike = { formId ->
                    viewModel.likeProfile(formId)
                },
                onDislike = { formId ->
                    viewModel.dislikeProfile(formId)
                },
                onNavigateToTest = { formId ->
                    viewModel.navigateToTest(formId)
                },
                onNavigateToChat = { chatId ->
                    viewModel.navigateToChat(chatId)
                },
                modifier = modifier
            )
        }
    }
}