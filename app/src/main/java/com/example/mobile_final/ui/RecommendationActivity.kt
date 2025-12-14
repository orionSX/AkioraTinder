package com.example.mobile_final.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.mobile_final.R
import com.example.mobile_final.dto.PlayerProfile
import com.example.mobile_final.services.ApiService
import com.example.mobile_final.services.ChatManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecommendationActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService
    private lateinit var chatManager: ChatManager
    private val scope = CoroutineScope(Dispatchers.Main)
    
    private lateinit var cardStackView: CardView
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileDescription: TextView
    private lateinit var likeButton: Button
    private lateinit var dislikeButton: Button
    
    private var profiles: MutableList<PlayerProfile> = mutableListOf()
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        apiService = ApiService.getInstance(this)
        chatManager = ChatManager.getInstance(this)
        
        setContentView(R.layout.activity_recommendation)
        
        initViews()
        loadRecommendedProfiles()
        
        setupSwipeListeners()
    }
    
    private fun initViews() {
        cardStackView = findViewById(R.id.card_stack_view)
        profileImage = findViewById(R.id.profile_image)
        profileName = findViewById(R.id.profile_name)
        profileDescription = findViewById(R.id.profile_description)
        likeButton = findViewById(R.id.like_button)
        dislikeButton = findViewById(R.id.dislike_button)
    }
    
    private fun setupSwipeListeners() {
        likeButton.setOnClickListener {
            if (currentIndex < profiles.size) {
                handleLike()
            }
        }
        
        dislikeButton.setOnClickListener {
            if (currentIndex < profiles.size) {
                handleDislike()
            }
        }
        
        // Add touch listeners for swipe gestures
        cardStackView.setOnTouchListener { _, event ->
            // Simple swipe detection would go here
            // For now, we'll just handle button clicks
            false
        }
    }
    
    private fun loadRecommendedProfiles() {
        scope.launch {
            try {
                val recommendedProfiles = withContext(Dispatchers.IO) {
                    apiService.getRecommendedForms()
                }
                
                profiles.clear()
                profiles.addAll(recommendedProfiles)
                currentIndex = 0
                
                if (profiles.isEmpty()) {
                    Toast.makeText(this@RecommendationActivity, "Нет доступных анкет", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    showCurrentProfile()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RecommendationActivity, "Ошибка загрузки анкет", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun showCurrentProfile() {
        if (currentIndex >= profiles.size) {
            Toast.makeText(this@RecommendationActivity, "Анкеты закончились", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        val profile = profiles[currentIndex]
        
        // Set profile information
        profileName.text = "${profile.userData.name}, ${profile.userData.age ?: "Возраст не указан"}"
        profileDescription.text = profile.description
        
        // Check if profile has a test
        if (profile.formTest != null && profile.formTest!!.questions.isNotEmpty()) {
            // This profile has a test, so we need to handle test passing
            handleProfileWithTest(profile)
        }
    }
    
    private fun handleProfileWithTest(profile: PlayerProfile) {
        // Convert questions to JSON string and pass to test activity
        val questionsJson = convertQuestionsToJson(profile.formTest!!.questions)
        val intent = Intent(this, TestActivity::class.java)
        intent.putExtra("PROFILE_ID", profile.id)
        intent.putExtra("TEST_QUESTIONS_JSON", questionsJson)
        intent.putExtra("THRESHOLD", profile.formTest!!.threshold)
        
        startActivityForResult(intent, 100) // Request code for test activity
    }
    
    private fun convertQuestionsToJson(questions: List<com.example.mobile_final.dto.Question>): String {
        val jsonArray = org.json.JSONArray()
        for (question in questions) {
            val jsonObj = org.json.JSONObject()
            jsonObj.put("question", question.question)
            jsonObj.put("answer", question.answer.toString().lowercase())
            jsonArray.put(jsonObj)
        }
        return jsonArray.toString()
    }
    
    private fun handleLike() {
        if (currentIndex >= profiles.size) return
        
        val profileId = profiles[currentIndex].id
        scope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    apiService.likeForm(profileId)
                }
                
                if (success) {
                    // Check if there's a chat response after liking
                    checkForChatAfterAction(profileId)
                    
                    currentIndex++
                    if (currentIndex < profiles.size) {
                        showCurrentProfile()
                    } else {
                        Toast.makeText(this@RecommendationActivity, "Анкеты закончились", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@RecommendationActivity, "Ошибка при лайке", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RecommendationActivity, "Ошибка при лайке", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun handleDislike() {
        if (currentIndex >= profiles.size) return
        
        val profileId = profiles[currentIndex].id
        scope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    apiService.dislikeForm(profileId)
                }
                
                if (success) {
                    currentIndex++
                    if (currentIndex < profiles.size) {
                        showCurrentProfile()
                    } else {
                        Toast.makeText(this@RecommendationActivity, "Анкеты закончились", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@RecommendationActivity, "Ошибка при дизлайке", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RecommendationActivity, "Ошибка при дизлайке", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun checkForChatAfterAction(profileId: String) {
        // After liking, check if a chat was created with the other user
        // Implementation depends on the API response structure
        // For now, we'll assume the API handles the matching logic server-side
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == 100) { // Test activity result
            if (resultCode == RESULT_OK) {
                // Test passed - create chat with the profile owner
                val profileId = data?.getStringExtra("PROFILE_ID")
                val chatCreated = data?.getBooleanExtra("CHAT_CREATED", false) ?: false
                
                if (chatCreated) {
                    val chatId = data?.getStringExtra("CHAT_ID")
                    if (!chatId.isNullOrEmpty()) {
                        // Navigate to chat activity
                        val intent = Intent(this, ChatActivity::class.java)
                        intent.putExtra("chat_id", chatId)
                        startActivity(intent)
                    }
                } else {
                    // Continue to next profile
                    currentIndex++
                    if (currentIndex < profiles.size) {
                        showCurrentProfile()
                    } else {
                        Toast.makeText(this@RecommendationActivity, "Анкеты закончились", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                // Test failed or cancelled - continue to next profile
                currentIndex++
                if (currentIndex < profiles.size) {
                    showCurrentProfile()
                } else {
                    Toast.makeText(this@RecommendationActivity, "Анкеты закончились", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}