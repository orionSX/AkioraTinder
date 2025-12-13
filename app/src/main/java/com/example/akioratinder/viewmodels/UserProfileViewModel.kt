package com.example.akioratinder.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.akioratinder.data.UserProfile
import com.example.akioratinder.services.ProfilesManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel(userProfileStore: UserProfileStore) : ViewModel() {


    fun updateProfile(
        context: android.content.Context,
        summonerName: String,
        server: String,
        role: String,
        rankTier: String,
        rankDivision: String,
        bio: String,
        age: String,
        gender: String,
        playStyle: String,
        microphone: String,
        goals: String,
        playSchedule: String
    ) {
        viewModelScope.launch {
            val newProfile = UserProfile(
                summonerName = summonerName,
                server = server,
                role = role,
                rankTier = rankTier,
                rankDivision = rankDivision,
                bio = bio,
                age = age,
                gender = gender,
                playStyle = playStyle,
                microphone = microphone,
                goals = goals,
                playSchedule = playSchedule
            )

            ProfilesManager.saveUserProfile(context, newProfile)
        }
    }


    fun getCurrentUserProfileFlow(context: android.content.Context): StateFlow<UserProfile> {
        return ProfilesManager.getCurrentUserProfileFlow(context)
    }
    fun userProfileStore(): UserProfileStore {
        return userProfileStore();
    }



    fun getSelectionOptions(context: android.content.Context): SelectionOptions {
        val store = ProfilesManager.getUserProfileStoreDirect(context)
        return SelectionOptions(
            servers = store.servers,
            roles = store.roles,
            rankTiers = store.rankTiers,
            rankDivisions = store.rankDivisions,
            genders = store.genders,
            playStyles = store.playStyles,
            microphoneOptions = store.microphoneOptions,
            goals = store.goals,
            playSchedules = store.playSchedules
        )
    }
}


data class SelectionOptions(
    val servers: List<String>,
    val roles: List<String>,
    val rankTiers: List<String>,
    val rankDivisions: List<String>,
    val genders: List<String>,
    val playStyles: List<String>,
    val microphoneOptions: List<String>,
    val goals: List<String>,
    val playSchedules: List<String>
)