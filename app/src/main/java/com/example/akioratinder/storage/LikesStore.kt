package com.example.akioratinder.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.akioratinder.data.LikedProfile
import com.example.akioratinder.data.UserProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class LikesStore(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("likes_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_LIKED_PROFILES = "liked_profiles"
    }

    private val _likedProfiles = MutableStateFlow(loadLikedProfiles())
    val likedProfiles: StateFlow<List<LikedProfile>> = _likedProfiles


    private fun loadLikedProfiles(): List<LikedProfile> {
        val likedProfilesJson = prefs.getString(KEY_LIKED_PROFILES, null)
        return if (likedProfilesJson != null) {
            val type = object : TypeToken<List<LikedProfile>>() {}.type
            gson.fromJson(likedProfilesJson, type) ?: emptyList()
        } else {
            emptyList()
        }
    }


    private fun saveLikedProfiles(profiles: List<LikedProfile>) {
        val profilesJson = gson.toJson(profiles)
        prefs.edit().putString(KEY_LIKED_PROFILES, profilesJson).apply()
        _likedProfiles.value = profiles
    }

    fun addLike(profile: UserProfile) {
        val currentLikes = _likedProfiles.value.toMutableList()

        if (currentLikes.none { it.userProfile.summonerName == profile.summonerName }) {
            currentLikes.add(LikedProfile(profile))
            saveLikedProfiles(currentLikes)
        }
    }


    fun removeLike(profile: UserProfile) {
        val currentLikes = _likedProfiles.value.toMutableList()
        currentLikes.removeAll { it.userProfile.summonerName == profile.summonerName }
        saveLikedProfiles(currentLikes)
    }


    fun isProfileLiked(profile: UserProfile): Boolean {
        return _likedProfiles.value.any { it.userProfile.summonerName == profile.summonerName }
    }


    fun getLikesCount(): Int {
        return _likedProfiles.value.size
    }

    fun clearAllLikes() {
        saveLikedProfiles(emptyList())
    }
}