// Question.kt
package com.example.mobile_final.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val question: String = "",
    val answer: AnswerType = AnswerType.YES
)

enum class AnswerType {
    YES,
    NO
}