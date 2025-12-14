// QuestionViewModel.kt
package com.example.mobile_final.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobile_final.database.AppDatabase
import com.example.mobile_final.model.AnswerType
import com.example.mobile_final.model.Question
import com.example.mobile_final.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class QuestionViewModel(

    private val repository: QuestionRepository
) : ViewModel() {

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions

    private val _selectedQuestion = MutableStateFlow<Question?>(null)
    val selectedQuestion: StateFlow<Question?> = _selectedQuestion

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog

    init {
        loadQuestions()
    }

    fun loadQuestions() {
        viewModelScope.launch {
            repository.getAllQuestions().collect { questionsList ->
                _questions.value = questionsList
            }
        }
    }

    fun setSelectedQuestion(question: Question?) {
        _selectedQuestion.value = question
    }

    fun showDialog(show: Boolean) {
        _showDialog.value = show
    }

    fun addQuestion(questionText: String, answerType: AnswerType) {
        viewModelScope.launch {
            val question = Question(question = questionText, answer = answerType)
            repository.insertQuestion(question)
        }
    }

    fun updateQuestion(id: Long, questionText: String, answerType: AnswerType) {
        viewModelScope.launch {
            val question = Question(id = id, question = questionText, answer = answerType)
            repository.updateQuestion(question)
        }
    }

    fun deleteQuestion(question: Question) {
        viewModelScope.launch {
            repository.deleteQuestion(question)
        }
    }

    fun deleteAllQuestions() {
        viewModelScope.launch {
            repository.deleteAllQuestions()
        }
    }}
