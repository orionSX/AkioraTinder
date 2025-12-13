package com.example.akioratinder.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val code: String? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UpdateProfileRequest(
    val email: String? = null,
    val password: String? = null,
    val name: String? = null,
    val age: Int? = null,
    val gender: Gender? = null,
    val discord: String? = null
)

@Serializable
data class CreateFormRequest(
    val description: String? = null,
    val account: Account,
    val roles: List<Role>,
    val rolesLookingFor: List<Role>,
    val personData: PersonData,
    val creatorId: String,
    val gameTypes: List<GameType>
)

@Serializable
data class UpdateFormRequest(
    val description: String? = null,
    val account: Account? = null,
    val roles: List<Role>? = null,
    val rolesLookingFor: List<Role>? = null,
    val personData: PersonData? = null,
    val gameTypes: List<GameType>? = null
)