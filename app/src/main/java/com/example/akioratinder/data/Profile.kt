package com.example.akioratinder.data




import java.io.Serializable


data class Profile(
    val id: String,
    val nickname: String,
    val server: String,
    val role: String,
    val rank: String,
    val about: String
) : Serializable