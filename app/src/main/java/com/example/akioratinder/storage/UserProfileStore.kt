package com.example.akioratinder.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.akioratinder.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserProfileStore(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)

    // Selection options как свойства класса
    val servers = listOf("EUW", "NA", "KR", "RU", "EUNE", "BR", "TR", "LAN", "LAS", "OCE", "JP")

    val roles = listOf("Top", "Jungle", "Mid", "ADC", "Support", "Fill")

    val rankTiers = listOf("Iron", "Bronze", "Silver", "Gold", "Platinum", "Diamond", "Master", "Grandmaster", "Challenger")

    val rankDivisions = listOf("IV", "III", "II", "I")

    val genders = listOf("Male", "Female", "Other", "Prefer not to say")

    val playStyles = listOf("Aggressive", "Passive", "Strategic", "Roaming", "Farming", "Team player")

    val microphoneOptions = listOf("Yes", "No", "Sometimes")

    val goals = listOf("Ranked climbing", "Casual fun", "Learning", "Tournaments", "Making friends")

    val playSchedules = listOf("Morning", "Afternoon", "Evening", "Night", "Weekends", "Flexible")

    companion object {
        // Keys
        private const val KEY_SUMMONER_NAME = "summoner_name"
        private const val KEY_SERVER = "server"
        private const val KEY_ROLE = "role"
        private const val KEY_RANK_TIER = "rank_tier"
        private const val KEY_RANK_DIVISION = "rank_division"
        private const val KEY_BIO = "bio"
        private const val KEY_AGE = "age"
        private const val KEY_GENDER = "gender"
        private const val KEY_PLAY_STYLE = "play_style"
        private const val KEY_MICROPHONE = "microphone"
        private const val KEY_GOALS = "goals"
        private const val KEY_PLAY_SCHEDULE = "play_schedule"

        // Default values
        private const val DEFAULT_SUMMONER_NAME = ""
        private const val DEFAULT_SERVER = "EUW"
        private const val DEFAULT_ROLE = "Mid"
        private const val DEFAULT_RANK_TIER = "Gold"
        private const val DEFAULT_RANK_DIVISION = "IV"
        private const val DEFAULT_BIO = ""
        private const val DEFAULT_AGE = ""
        private const val DEFAULT_GENDER = ""
        private const val DEFAULT_PLAY_STYLE = ""
        private const val DEFAULT_MICROPHONE = ""
        private const val DEFAULT_GOALS = ""
        private const val DEFAULT_PLAY_SCHEDULE = ""
    }

    private val _userProfile = MutableStateFlow(loadUserProfile())
    val userProfileFlow: StateFlow<UserProfile> = _userProfile

    private fun loadUserProfile(): UserProfile {
        return UserProfile(
            summonerName = prefs.getString(KEY_SUMMONER_NAME, DEFAULT_SUMMONER_NAME) ?: DEFAULT_SUMMONER_NAME,
            server = prefs.getString(KEY_SERVER, DEFAULT_SERVER) ?: DEFAULT_SERVER,
            role = prefs.getString(KEY_ROLE, DEFAULT_ROLE) ?: DEFAULT_ROLE,
            rankTier = prefs.getString(KEY_RANK_TIER, DEFAULT_RANK_TIER) ?: DEFAULT_RANK_TIER,
            rankDivision = prefs.getString(KEY_RANK_DIVISION, DEFAULT_RANK_DIVISION) ?: DEFAULT_RANK_DIVISION,
            bio = prefs.getString(KEY_BIO, DEFAULT_BIO) ?: DEFAULT_BIO,
            age = prefs.getString(KEY_AGE, DEFAULT_AGE) ?: DEFAULT_AGE,
            gender = prefs.getString(KEY_GENDER, DEFAULT_GENDER) ?: DEFAULT_GENDER,
            playStyle = prefs.getString(KEY_PLAY_STYLE, DEFAULT_PLAY_STYLE) ?: DEFAULT_PLAY_STYLE,
            microphone = prefs.getString(KEY_MICROPHONE, DEFAULT_MICROPHONE) ?: DEFAULT_MICROPHONE,
            goals = prefs.getString(KEY_GOALS, DEFAULT_GOALS) ?: DEFAULT_GOALS,
            playSchedule = prefs.getString(KEY_PLAY_SCHEDULE, DEFAULT_PLAY_SCHEDULE) ?: DEFAULT_PLAY_SCHEDULE
        )
    }

    fun saveUserProfile(profile: UserProfile) {
        prefs.edit().apply {
            putString(KEY_SUMMONER_NAME, profile.summonerName)
            putString(KEY_SERVER, profile.server)
            putString(KEY_ROLE, profile.role)
            putString(KEY_RANK_TIER, profile.rankTier)
            putString(KEY_RANK_DIVISION, profile.rankDivision)
            putString(KEY_BIO, profile.bio)
            putString(KEY_AGE, profile.age)
            putString(KEY_GENDER, profile.gender)
            putString(KEY_PLAY_STYLE, profile.playStyle)
            putString(KEY_MICROPHONE, profile.microphone)
            putString(KEY_GOALS, profile.goals)
            putString(KEY_PLAY_SCHEDULE, profile.playSchedule)
            apply()
        }
        _userProfile.value = profile
    }

    fun getUserProfile(): UserProfile = loadUserProfile()
}