package com.example.akioratinder.storage

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionSwipeStore(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("session_swipes", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SWIPED_LEFT = "swiped_left"
        private const val KEY_SWIPED_RIGHT = "swiped_right"
    }

    private val _swipedLeft = MutableStateFlow(loadSwipedLeft())
    private val _swipedRight = MutableStateFlow(loadSwipedRight())

    val swipedLeft: StateFlow<Set<String>> = _swipedLeft
    val swipedRight: StateFlow<Set<String>> = _swipedRight

    private fun loadSwipedLeft(): Set<String> {
        return prefs.getStringSet(KEY_SWIPED_LEFT, emptySet()) ?: emptySet()
    }

    private fun loadSwipedRight(): Set<String> {
        return prefs.getStringSet(KEY_SWIPED_RIGHT, emptySet()) ?: emptySet()
    }

    private fun saveSwipedLeft(swipedLeft: Set<String>) {
        prefs.edit().putStringSet(KEY_SWIPED_LEFT, swipedLeft).apply()
        _swipedLeft.value = swipedLeft
    }

    private fun saveSwipedRight(swipedRight: Set<String>) {
        prefs.edit().putStringSet(KEY_SWIPED_RIGHT, swipedRight).apply()
        _swipedRight.value = swipedRight
    }

    fun markLeft(summonerName: String) {
        val currentSwipedLeft = _swipedLeft.value.toMutableSet()
        currentSwipedLeft.add(summonerName)
        saveSwipedLeft(currentSwipedLeft)
    }

    fun markRight(summonerName: String) {
        val currentSwipedRight = _swipedRight.value.toMutableSet()
        currentSwipedRight.add(summonerName)
        saveSwipedRight(currentSwipedRight)
    }

    fun wasSwipedLeft(summonerName: String): Boolean {
        return summonerName in _swipedLeft.value
    }

    fun wasSwipedRight(summonerName: String): Boolean {
        return summonerName in _swipedRight.value
    }

    fun wasSwiped(summonerName: String): Boolean {
        return wasSwipedLeft(summonerName) || wasSwipedRight(summonerName)
    }

    fun clearAll() {
        saveSwipedLeft(emptySet())
        saveSwipedRight(emptySet())
    }

    fun clearForUser(summonerName: String) {
        val currentSwipedLeft = _swipedLeft.value.toMutableSet()
        val currentSwipedRight = _swipedRight.value.toMutableSet()

        currentSwipedLeft.remove(summonerName)
        currentSwipedRight.remove(summonerName)

        saveSwipedLeft(currentSwipedLeft)
        saveSwipedRight(currentSwipedRight)
    }
}