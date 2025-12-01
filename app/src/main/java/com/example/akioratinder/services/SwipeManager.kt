package com.example.akioratinder.services

import android.content.Context
import com.example.akioratinder.data.UserProfile
import com.example.akioratinder.storage.SessionSwipeStore

object SwipeManager {
    private var swipeStore: SessionSwipeStore? = null

    fun initialize(context: Context) {
        if (swipeStore == null) {
            swipeStore = SessionSwipeStore(context)
        }
    }

    private fun getStore(context: Context): SessionSwipeStore {
        if (swipeStore == null) {
            initialize(context)
        }
        return swipeStore!!
    }

    // Метод для получения непросмотренных профилей
    fun getUnswipedProfiles(context: Context, allProfiles: List<UserProfile>): List<UserProfile> {
        val store = getStore(context)
        return allProfiles.filter { !store.wasSwiped(it.summonerName) }
    }

    // Свайп влево
    fun swipeLeft(context: Context, profile: UserProfile) {
        getStore(context).markLeft(profile.summonerName)
    }

    // Свайп вправо (лайк)
    fun swipeRight(context: Context, profile: UserProfile) {
        getStore(context).markRight(profile.summonerName)
        // Также добавляем в лайки
        LikesManager.addLike(context, profile)
    }

    // Проверка был ли свайп
    fun wasSwiped(context: Context, summonerName: String): Boolean {
        return getStore(context).wasSwiped(summonerName)
    }

    // Получение всех свайпнутых влево
    fun getSwipedLeft(context: Context): Set<String> {
        return getStore(context).swipedLeft.value
    }

    // Получение всех свайпнутых вправо
    fun getSwipedRight(context: Context): Set<String> {
        return getStore(context).swipedRight.value
    }

    // Очистка свайпов
    fun clearSwipes(context: Context) {
        getStore(context).clearAll()
    }
}