package com.example.akioratinder.services



import android.content.Context
import com.example.akioratinder.data.LikedProfile
import com.example.akioratinder.data.UserProfile
import kotlinx.coroutines.flow.StateFlow


object LikesManager {
    private var likesStore: LikesStore? = null

    fun initialize(context: Context) {
        if (likesStore == null) {
            likesStore = LikesStore(context)
        }
    }

    private fun getStore(context: Context): LikesStore {
        if (likesStore == null) {
            initialize(context)
        }
        return likesStore!!
    }


    fun addLike(context: Context, profile: UserProfile) {
        getStore(context).addLike(profile)
    }


    fun removeLike(context: Context, profile: UserProfile) {
        getStore(context).removeLike(profile)
    }


    fun getLikedProfiles(context: Context): StateFlow<List<LikedProfile>> {
        return getStore(context).likedProfiles
    }


    fun isProfileLiked(context: Context, profile: UserProfile): Boolean {
        return getStore(context).isProfileLiked(profile)
    }


    fun getLikesCount(context: Context): Int {
        return getStore(context).getLikesCount()
    }


    fun clearAllLikes(context: Context) {
        getStore(context).clearAllLikes()
    }


    fun getLikesStoreDirect(context: Context): LikesStore {
        return getStore(context)
    }
}