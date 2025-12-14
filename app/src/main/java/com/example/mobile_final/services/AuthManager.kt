package com.example.mobile_final.services



import android.content.Context
import android.content.SharedPreferences
import com.example.mobile_final.dto.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthManager private constructor(context: Context) {
    private val prefs: SharedPreferences
    private val apiService: ApiService

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _currentPlayerProfile = MutableStateFlow<PlayerProfile?>(null)
    val currentPlayerProfile: StateFlow<PlayerProfile?> = _currentPlayerProfile.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        apiService = ApiService.getInstance(context)
        restoreSession()
    }

    companion object {
        @Volatile
        private var INSTANCE: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthManager(context).also { INSTANCE = it }
            }
        }
    }

    suspend fun login(email: String, password: String): AuthResponse {
        try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)

            if (response.success) {

                saveSession( response.user!!)
                _currentUser.value = response.user
                _isLoggedIn.value = true
                loadPlayerProfile()

                return response
            }
            else{
                return AuthResponse(false,null)
            }

        } catch (e: Exception) {

            return AuthResponse(false,null)
        }

    }

    private fun isDemoAccount(email: String, password: String): Boolean {
        val demoAccounts = mapOf(
            "user1@example.com" to "password123",
            "user2@example.com" to "password123",
            "test@test.com" to "123456",
            "demo@demo.com" to "demo123"
        )
        return demoAccounts[email] == password
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,

    ): Boolean {
        val request = RegisterRequest(name, email, password)
        val started = apiService.register(request)

        return started
    }
    suspend fun verify(

        email: String,
        code: String,

        ): AuthResponse {
        val request = VerifyRequest( email, code)
        val response = apiService.verify(request)

        if (response.success && response.user != null) {
            saveSession(response.user!!)
            _currentUser.value = response.user
            _isLoggedIn.value = true
        }
        return response
    }

    suspend fun updateProfile(update: UpdateProfileRequest): UserProfile {
        val userId = _currentUser.value?.id ?: throw Exception("User not authenticated")
        val user = apiService.updateUser(userId, update)
        _currentUser.value = user
        saveUserData(user)
        return user
    }

    suspend fun loadPlayerProfile() {
        try {
            val forms = apiService.getForms()
            val currentUserId = _currentUser.value?.id
            if (currentUserId != null) {
                val userProfile = forms.find { it.creatorId == currentUserId }
                _currentPlayerProfile.value = userProfile
            }
        } catch (e: Exception) {
            // Профиль игрока может не существовать - это нормально
            _currentPlayerProfile.value = null
        }
    }

    suspend fun createPlayerProfile(formData: CreateFormRequest): PlayerProfile? {
        val profile = apiService.createForm(formData)
        if (profile != null) {
            _currentPlayerProfile.value = profile
        }
        return profile
    }

    suspend fun updatePlayerProfile(formId: String, update: UpdateFormRequest): PlayerProfile? {
        val profile = apiService.updateForm(formId, update)
        if (profile != null) {
            _currentPlayerProfile.value = profile
        }
        return profile
    }

    fun logout() {
        clearSession()
        apiService.clearAuthToken()
        _currentUser.value = null
        _currentPlayerProfile.value = null
        _isLoggedIn.value = false
    }

    private fun saveSession( user: UserProfile) {
        prefs.edit()

            .putString("user_id", user.id)
            .putString("user_name", user.name)
            .putString("user_email", user.email)
            .apply()
    }

    private fun saveUserData(user: UserProfile) {
        prefs.edit()
            .putString("user_id", user.id)
            .putString("user_name", user.name)
            .putString("user_email", user.email)
            .apply()
    }

    private fun restoreSession() {



        val userId = prefs.getString("user_id", "")
        val userName = prefs.getString("user_name", "")
        val userEmail = prefs.getString("user_email", "")

        if (userId?.isNotEmpty() == true) {
            val user = UserProfile(
                id = userId,
                name = userName ?: "",
                email = userEmail ?: "",
                age = null,
                gender = null,
                discord = null,
                role = "user"
            )
            _currentUser.value = user
            _isLoggedIn.value = true
        }

    }

    private fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }
}

// Упрощенная модель AuthResponse для работы с JSON
data class AuthResponse(
    val success: Boolean,

    val user: UserProfile?
)