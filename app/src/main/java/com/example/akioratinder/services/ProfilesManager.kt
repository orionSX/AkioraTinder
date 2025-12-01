package com.example.akioratinder.services

import android.content.Context
import com.example.akioratinder.data.UserProfile
import com.example.akioratinder.storage.ProfilesStore
import com.example.akioratinder.storage.UserProfileStore
import kotlinx.coroutines.flow.StateFlow


object ProfilesManager {
    private var profilesStore: ProfilesStore? = null
    private var userProfileStore: UserProfileStore? = null

    fun initialize(context: Context) {
        if (profilesStore == null) {
            profilesStore = ProfilesStore(context)
        }
        if (userProfileStore == null) {
            userProfileStore = UserProfileStore(context)
        }
    }

    private fun getProfilesStore(context: Context): ProfilesStore {
        if (profilesStore == null) {
            initialize(context)
        }
        return profilesStore!!
    }

    private fun getUserProfileStore(context: Context): UserProfileStore {
        if (userProfileStore == null) {
            initialize(context)
        }
        return userProfileStore!!
    }


    fun saveUserProfile(context: Context, profile: UserProfile) {
        val userStore = getUserProfileStore(context)
        val profilesStore = getProfilesStore(context)


        userStore.saveUserProfile(profile)


        val existingProfile = profilesStore.getProfile(profile.summonerName)
        if (existingProfile == null) {

            profilesStore.addProfile(profile)
        } else {

            profilesStore.updateProfile(profile)
        }
    }


    fun getCurrentUserProfile(context: Context): UserProfile {
        return getUserProfileStore(context).getUserProfile()
    }


    fun getCurrentUserProfileFlow(context: Context): StateFlow<UserProfile> {
        return getUserProfileStore(context).userProfileFlow
    }


    fun getAllProfiles(context: Context): StateFlow<List<UserProfile>> {
        return getProfilesStore(context).profiles
    }


    fun getProfilesStoreDirect(context: Context): ProfilesStore {
        return getProfilesStore(context)
    }


    fun getUserProfileStoreDirect(context: Context): UserProfileStore {
        return getUserProfileStore(context)
    }


    fun removeProfile(context: Context, profile: UserProfile) {
        getProfilesStore(context).removeProfile(profile)
    }


    fun findProfile(context: Context, summonerName: String): UserProfile? {
        return getProfilesStore(context).getProfile(summonerName)
    }
}