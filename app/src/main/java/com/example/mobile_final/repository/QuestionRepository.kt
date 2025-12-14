// QuestionRepository.kt
package com.example.mobile_final.repository

import com.example.mobile_final.dao.QuestionDao
import com.example.mobile_final.model.Question
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class QuestionRepository @Inject constructor(
    private val questionDao: QuestionDao
) {
    fun getAllQuestions(): Flow<List<Question>> = questionDao.getAllQuestions()

    suspend fun getQuestionById(id: Long): Question? = questionDao.getQuestionById(id)

    suspend fun insertQuestion(question: Question): Long = questionDao.insertQuestion(question)

    suspend fun updateQuestion(question: Question) = questionDao.updateQuestion(question)

    suspend fun deleteQuestion(question: Question) = questionDao.deleteQuestion(question)

    suspend fun deleteAllQuestions() = questionDao.deleteAllQuestions()
}