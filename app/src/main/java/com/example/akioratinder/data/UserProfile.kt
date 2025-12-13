package com.example.akioratinder.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class UserProfile(
    @SerialName("_id") val id: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int? = null,
    val gender: Gender? = null,
    val discord: String? = null,
    val role: String = "user"
)

@Serializable
enum class Gender {
    @SerialName("male") MALE,
    @SerialName("female") FEMALE,
    @SerialName("any") ANY
}