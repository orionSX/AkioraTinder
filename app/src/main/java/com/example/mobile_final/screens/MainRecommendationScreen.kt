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

    // Обработка состояний навигации
    LaunchedEffect(uiState) {
        when (uiState) {
            is RecommendationViewModel.UiState.NavigateToChat -> {
                val chatId = (uiState as RecommendationViewModel.UiState.NavigateToChat).chatId
                onNavigateToChat(chatId)
                viewModel.resetNavigationState()
            }
            is RecommendationViewModel.UiState.NavigateToTest -> {
                // Проверим, существует ли профиль с тестом
                val formId = (uiState as RecommendationViewModel.UiState.NavigateToTest).formId
                val profile = profiles.find { it.id == formId }

                if (profile == null || profile.formTest == null) {
                    // Если профиль не найден или нет теста, вернуться к рекомендациям
                    viewModel.navigateBackToRecommendations()
                }
            }
            is RecommendationViewModel.UiState.ShowTestResult -> {
                val testState = uiState as RecommendationViewModel.UiState.ShowTestResult

                // Обработка результата теста
                if (testState.success) {
                    // Успешно пройден тест - проверяем, есть ли информация о чате в сообщении
                    val message = testState.message ?: ""

                    try {
                        // Пытаемся разобрать сообщение как JSON
                        if (message.isNotEmpty() && (message.contains("{") || message.contains("chat_id") || message.contains("_id"))) {
                            val jsonResponse = JSONObject(message)

                            // Проверяем различные возможные форматы ответа
                            if (jsonResponse.has("_id") && jsonResponse.has("user_1")) {
                                // Это объект чата, извлекаем ID чата
                                val chatId = jsonResponse.optString("_id", "")
                                if (chatId.isNotEmpty()) {
                                    viewModel.navigateToChat(chatId)
                                } else {
                                    viewModel.navigateBackToRecommendations()
                                }
                            } else {
                                // Проверяем другие возможные форматы
                                val chatId = jsonResponse.optString("chat_id",
                                    jsonResponse.optString("chatId",
                                        jsonResponse.optString("id", "")))

                                if (chatId.isNotEmpty()) {
                                    viewModel.navigateToChat(chatId)
                                } else {
                                    viewModel.navigateBackToRecommendations()
                                }
                            }
                        } else {
                            // Если это не JSON, проверяем, есть ли ID чата в тексте
                            if (message.contains("chat", ignoreCase = true)) {
                                // Можно попытаться извлечь ID чата из текста
                                val regex = "chat[_-]?id[=:]\\s*([\\w-]+)".toRegex(RegexOption.IGNORE_CASE)
                                val matchResult = regex.find(message)
                                val chatId = matchResult?.groupValues?.getOrNull(1)

                                if (!chatId.isNullOrEmpty()) {
                                    viewModel.navigateToChat(chatId)
                                } else {
                                    viewModel.navigateBackToRecommendations()
                                }
                            } else {
                                viewModel.navigateBackToRecommendations()
                            }
                        }
                    } catch (e: Exception) {
                        // Если парсинг JSON не удался
                        viewModel.navigateBackToRecommendations()
                    }
                } else {
                    // Тест не пройден - возвращаемся к рекомендациям
                    viewModel.navigateBackToRecommendations()
                }
            }
            else -> {}
        }
    }

    // Отображение текущего состояния
    when (val currentState = uiState) {
        is RecommendationViewModel.UiState.NavigateToTest -> {
            val formId = currentState.formId
            val profile = profiles.find { it.id == formId }

            if (profile != null && profile.formTest != null) {
                TestScreen(
                    questions = profile.formTest.questions,
                    onSubmit = { answers ->

                        viewModel.passTest(formId = formId, answers = answers)
                    },
                    onBack = {
                        viewModel.resetNavigationState()
                    }
                )
            } else {
                // Если профиль или тест не найден, возвращаемся к рекомендациям
                LaunchedEffect(Unit) {
                    viewModel.navigateBackToRecommendations()
                }

                // Показываем загрузку или ошибку
                RecommendationScreen(
                    profiles = emptyList(),
                    onLike = { /* */ },
                    onDislike = { /* */ },
                    onNavigateToTest = { /* */ },
                    onNavigateToChat = { /* */ },
                    modifier = modifier
                )
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