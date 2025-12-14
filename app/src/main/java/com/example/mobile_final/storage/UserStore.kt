package com.example.mobile_final.storage



import android.content.Context
import android.content.SharedPreferences
import com.example.mobile_final.dto.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserStore(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_AGE = "user_age"
        private const val KEY_USER_GENDER = "user_gender"
        private const val KEY_USER_DISCORD = "user_discord"
        private const val KEY_USER_ROLE = "user_role"
    }

    private val _userData = MutableStateFlow(getUserData())
    val userDataFlow: StateFlow<UserProfile?> = _userData

    fun getUserData(): UserProfile? {
        val id = prefs.getString(KEY_USER_ID, null)
        val name = prefs.getString(KEY_USER_NAME, null)
        val email = prefs.getString(KEY_USER_EMAIL, null)
        val age = if (prefs.contains(KEY_USER_AGE)) prefs.getInt(KEY_USER_AGE, 0) else null
        val genderString = prefs.getString(KEY_USER_GENDER, null)
        val discord = prefs.getString(KEY_USER_DISCORD, null)
        val role = prefs.getString(KEY_USER_ROLE, "user")

        if (id == null) return null

        val gender = when (genderString?.lowercase()) {
            "male" -> com.example.mobile_final.dto.Gender.MALE
            "female" -> com.example.mobile_final.dto.Gender.FEMALE
            "any" -> com.example.mobile_final.dto.Gender.ANY
            else -> null
        }

        return UserProfile(
            id = id,
            name = name ?: "",
            email = email ?: "",
            age = age,
            gender = gender,
            discord = discord,
            role = role ?: "user"
        )
    }

    fun saveUserData(user: UserProfile) {
        prefs.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_EMAIL, user.email)
            .apply {
                if (user.age != null) putInt(KEY_USER_AGE, user.age) else remove(KEY_USER_AGE)
            }
            .apply {
                if (user.gender != null) putString(KEY_USER_GENDER, user.gender.toString().lowercase()) else remove(KEY_USER_GENDER)
            }
            .apply {
                if (user.discord != null) putString(KEY_USER_DISCORD, user.discord) else remove(KEY_USER_DISCORD)
            }
            .putString(KEY_USER_ROLE, user.role)
            .apply()

        _userData.value = user
    }

    fun clearUserData() {
        prefs.edit().clear().apply()
        _userData.value = null
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
}