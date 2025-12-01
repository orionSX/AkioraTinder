package com.example.akioratinder.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.akioratinder.data.UserProfile

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfilesStore(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("profiles_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_PROFILES_LIST = "profiles_list"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
    }

    private val _profiles = MutableStateFlow(loadProfiles())
    val profiles: StateFlow<List<UserProfile>> = _profiles


    private fun loadProfiles(): List<UserProfile> {
        val profilesJson = prefs.getString(KEY_PROFILES_LIST, null)
        return if (profilesJson != null) {
            val type = object : TypeToken<List<UserProfile>>() {}.type
            gson.fromJson(profilesJson, type) ?: getDefaultProfiles()
        } else {
            getDefaultProfiles()
        }
    }


    fun saveProfiles(profiles: List<UserProfile>) {
        val profilesJson = gson.toJson(profiles)
        prefs.edit().putString(KEY_PROFILES_LIST, profilesJson).apply()
        _profiles.value = profiles
    }


    fun addProfile(profile: UserProfile) {
        val currentProfiles = _profiles.value.toMutableList()
        currentProfiles.add(profile)
        saveProfiles(currentProfiles)
    }


    fun updateProfile(updatedProfile: UserProfile) {
        val currentProfiles = _profiles.value.toMutableList()
        val index = currentProfiles.indexOfFirst { it.summonerName == updatedProfile.summonerName }
        if (index != -1) {
            currentProfiles[index] = updatedProfile
            saveProfiles(currentProfiles)
        }
    }


    fun removeProfile(profile: UserProfile) {
        val currentProfiles = _profiles.value.toMutableList()
        currentProfiles.removeAll { it.summonerName == profile.summonerName }
        saveProfiles(currentProfiles)
    }


    fun getProfile(summonerName: String): UserProfile? {
        return _profiles.value.find { it.summonerName == summonerName }
    }


    private fun getDefaultProfiles(): List<UserProfile> {
        return listOf(
            UserProfile(
                summonerName = "AhriQueen",
                server = "EUW",
                role = "Mid",
                rankTier = "Diamond",
                rankDivision = "II",
                bio = "Люблю играть в команде, предпочитаю контрольные мид-пикеры",
                age = "25",
                gender = "Female",
                playStyle = "Strategic",
                microphone = "Yes",
                goals = "Ranked climbing",
                playSchedule = "Evening"
            ),
            UserProfile(
                summonerName = "LeeSinMaster",
                server = "NA",
                role = "Jungle",
                rankTier = "Platinum",
                rankDivision = "I",
                bio = "Опытный джанглер, ищу тиммейтов для клаша",
                age = "28",
                gender = "Male",
                playStyle = "Aggressive",
                microphone = "Yes",
                goals = "Tournaments",
                playSchedule = "Weekends"
            ),
            UserProfile(
                summonerName = "JinxFanatic",
                server = "KR",
                role = "ADC",
                rankTier = "Gold",
                rankDivision = "III",
                bio = "Весёлый ADC, люблю фановый дэмедж и агрессивную игру",
                age = "22",
                gender = "Female",
                playStyle = "Aggressive",
                microphone = "Sometimes",
                goals = "Casual fun",
                playSchedule = "Flexible"
            ),
            UserProfile(
                summonerName = "BardSupport",
                server = "EUW",
                role = "Support",
                rankTier = "Platinum",
                rankDivision = "IV",
                bio = "Играю на саппорте, всегда помогаю команде и создаю пространство",
                age = "26",
                gender = "Male",
                playStyle = "Roaming",
                microphone = "Yes",
                goals = "Making friends",
                playSchedule = "Afternoon"
            ),
            UserProfile(
                summonerName = "ZedShadow",
                server = "RU",
                role = "Mid",
                rankTier = "Diamond",
                rankDivision = "I",
                bio = "Люблю соло-мид и агрессивный стиль, специализируюсь на ассасинах",
                age = "24",
                gender = "Male",
                playStyle = "Aggressive",
                microphone = "Yes",
                goals = "Ranked climbing",
                playSchedule = "Night"
            ),
            UserProfile(
                summonerName = "ThreshHook",
                server = "EUW",
                role = "Support",
                rankTier = "Gold",
                rankDivision = "II",
                bio = "Обожаю ловить флеши крюком, играю на предикшене",
                age = "29",
                gender = "Male",
                playStyle = "Strategic",
                microphone = "No",
                goals = "Learning",
                playSchedule = "Evening"
            ),
            UserProfile(
                summonerName = "EzrealSniper",
                server = "NA",
                role = "ADC",
                rankTier = "Platinum",
                rankDivision = "III",
                bio = "Механики выше среднего, специализируюсь на скиллшот AD керри",
                age = "23",
                gender = "Male",
                playStyle = "Farming",
                microphone = "Sometimes",
                goals = "Tournaments",
                playSchedule = "Weekends"
            )
        )
    }
}