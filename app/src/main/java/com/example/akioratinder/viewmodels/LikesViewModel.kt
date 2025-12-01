package com.example.akioratinder.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.akioratinder.data.LikedProfile
import com.example.akioratinder.services.LikesManager
import kotlinx.coroutines.flow.StateFlow

class LikesViewModel : ViewModel() {

    // Получение Flow лайкнутых профилей
    fun getLikedProfilesFlow(context: Context): StateFlow<List<LikedProfile>> {
        return LikesManager.getLikedProfiles(context)
    }

    // Удаление лайка
    fun removeLike(context: Context, profile: com.example.akioratinder.data.UserProfile) {
        LikesManager.removeLike(context, profile)
    }

    // Получение количества лайков
    fun getLikesCount(context: Context): Int {
        return LikesManager.getLikesCount(context)
    }

    // Очистка всех лайков
    fun clearAllLikes(context: Context) {
        LikesManager.clearAllLikes(context)
    }
}