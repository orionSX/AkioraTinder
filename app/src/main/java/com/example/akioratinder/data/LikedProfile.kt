package com.example.akioratinder.data

import java.util.*

data class LikedProfile(
    val userProfile: UserProfile,
    val likedAt: Date = Date(),
    val isMatch: Boolean = false
)