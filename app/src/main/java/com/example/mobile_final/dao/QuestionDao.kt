// QuestionDao.kt
package com.example.mobile_final.dao

import androidx.room.*
import com.example.mobile_final.model.Question
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY id DESC")
    fun getAllQuestions(): Flow<List<Question>>


    @Query("SELECT * FROM questions ORDER BY id DESC")
    fun getAllQuestionsList(): List<Question>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionById(id: Long): Question?

    @Insert
    suspend fun insertQuestion(question: Question): Long

    @Update
    suspend fun updateQuestion(question: Question)

    @Delete
    suspend fun deleteQuestion(question: Question)

    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions()
}