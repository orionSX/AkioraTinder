package com.example.mobile_final.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_final.R
import com.example.mobile_final.dto.Question
import com.example.mobile_final.dto.Answer
import com.example.mobile_final.services.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class TestActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private val scope = CoroutineScope(Dispatchers.Main)
    
    private lateinit var questionText: TextView
    private lateinit var yesRadioButton: RadioButton
    private lateinit var noRadioButton: RadioButton
    private lateinit var radioGroup: RadioGroup
    private lateinit var submitButton: Button
    
    private var questions: List<Question> = emptyList()
    private var profileId: String = ""
    private var threshold: Int = 2
    
    private var currentQuestionIndex = 0
    private val userAnswers: MutableList<Answer> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        apiService = ApiService.getInstance(this)
        
        setContentView(R.layout.activity_test)
        
        // Get data from intent
        profileId = intent.getStringExtra("PROFILE_ID") ?: ""
        threshold = intent.getIntExtra("THRESHOLD", 2)
        
        // Parse questions from JSON string
        val questionsJsonString = intent.getStringExtra("TEST_QUESTIONS_JSON") ?: ""
        questions = parseQuestionsFromJson(questionsJsonString)
        
        if (questions.isEmpty()) {
            Toast.makeText(this, "Нет вопросов в тесте", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        
        initViews()
        showCurrentQuestion()
    }
    
    private fun parseQuestionsFromJson(jsonString: String): List<Question> {
        try {
            val jsonArray = JSONArray(jsonString)
            val questionList = mutableListOf<Question>()
            
            for (i in 0 until jsonArray.length()) {
                val questionObj = jsonArray.getJSONObject(i)
                val questionText = questionObj.getString("question")
                val answerStr = questionObj.getString("answer")
                val answer = if (answerStr.lowercase() == "yes") Answer.YES else Answer.NO
                
                questionList.add(Question(questionText, answer))
            }
            
            return questionList
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
    
    private fun initViews() {
        questionText = findViewById(R.id.question_text)
        yesRadioButton = findViewById(R.id.yes_radio_button)
        noRadioButton = findViewById(R.id.no_radio_button)
        radioGroup = findViewById(R.id.radio_group)
        submitButton = findViewById(R.id.submit_button)
        
        submitButton.setOnClickListener {
            handleSubmit()
        }
    }
    
    private fun showCurrentQuestion() {
        if (currentQuestionIndex >= questions.size) {
            // All questions answered, submit test
            submitTest()
            return
        }
        
        val currentQuestion = questions[currentQuestionIndex]
        questionText.text = currentQuestion.question
        
        // Clear selection
        radioGroup.clearCheck()
    }
    
    private fun handleSubmit() {
        val selectedId = radioGroup.checkedRadioButtonId
        
        if (selectedId == -1) {
            Toast.makeText(this, "Пожалуйста, выберите ответ", Toast.LENGTH_SHORT).show()
            return
        }
        
        val answer = if (selectedId == R.id.yes_radio_button) Answer.YES else Answer.NO
        userAnswers.add(answer)
        
        currentQuestionIndex++
        
        if (currentQuestionIndex < questions.size) {
            showCurrentQuestion()
        } else {
            // All questions answered, submit test
            submitTest()
        }
    }
    
    private fun submitTest() {
        scope.launch {
            try {
                // Submit test answers to the server
                val response = withContext(Dispatchers.IO) {
                    apiService.passTest(profileId, userAnswers)
                }
                
                // Process the response
                if (response.contains("chat")) {
                    // If response contains chat info, open chat activity
                    val chatId = extractChatId(response)
                    if (chatId.isNotEmpty()) {
                        val resultIntent = Intent()
                        resultIntent.putExtra("PROFILE_ID", profileId)
                        resultIntent.putExtra("CHAT_CREATED", true)
                        resultIntent.putExtra("CHAT_ID", chatId)
                        
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        // No chat created, continue with recommendations
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                } else {
                    // Test not passed, continue with recommendations
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TestActivity, "Ошибка при отправке теста", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }
    
    private fun extractChatId(response: String): String {
        // Simple parsing to extract chat ID from response
        // In a real implementation, this would use proper JSON parsing
        val regex = Regex("\"chat_id\":\\s*\"([^\"]+)\"")
        val match = regex.find(response)
        return match?.groupValues?.getOrNull(1) ?: ""
    }
}