package com.example.akioratinder.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class PlayerProfile(
    @SerialName("_id") val id: String = "",
    val creatorId: String,
    val account: Account,
    val description: String = "",
    val gameData: GameData,
    val personData: PersonData,
    val userData: UserData,
    val createdAt: String = "",
    val deleted: Boolean = false,
    val active: Boolean = false,
    val likedBy: List<String> = emptyList(),
    val dislikedBy: List<String> = emptyList(),
    val formTest: FormTest? = null,
    val testResults: Map<String, TestResult> = emptyMap()
)

@Serializable
data class Account(
    val name: String,
    val server: String,
    val tag: String
)

@Serializable
data class UserData(
    val age: Int? = null,
    val gender: Gender? = null,
    val name: String,
    val discord: String? = null
)

@Serializable
data class GameData(
    val roles: List<Role>,
    val rolesLookingFor: List<Role>,
    val stats: SummonerStats,
    val canPlayWith: Map<String, Any>? = null,
    val gameTypes: List<GameType>
)

@Serializable
data class PersonData(
    val minAge: Int? = null,
    val maxAge: Int? = null,
    val gender: Gender? = null,
    val voice: Boolean = false
)

@Serializable
enum class Role {
    @SerialName("top") TOP,
    @SerialName("jg") JG,
    @SerialName("mid") MID,
    @SerialName("adc") ADC,
    @SerialName("sup") SUP,
    @SerialName("any") ANY
}

@Serializable
enum class GameType {
    @SerialName("normal") NORMAL,
    @SerialName("aram") ARAM,
    @SerialName("arena") ARENA,
    @SerialName("soloq") SOLOQ,
    @SerialName("flex") FLEX,
    @SerialName("any") ANY
}

@Serializable
data class SummonerStats(
    val soloQueue: RankedStats? = null,
    val flexQueue: RankedStats? = null,
    val championStats: List<ChampionStats> = emptyList()
)

@Serializable
data class RankedStats(
    val currentRank: String = "",
    val currentLp: String = "",
    val winLoss: String = "",
    val winRate: String = "",
    val bestRank: String = "",
    val bestLp: String = "",
    val iconId: Int? = null
)

@Serializable
data class ChampionStats(
    val position: String = "",
    val champion: String = "",
    val wins: String = "",
    val losses: String = "",
    val winRate: String = "",
    val kdaRatio: String = "",
    val kda: String = "",
    val laning: String = "",
    val damagePerMinute: String = "",
    val damageShareRatio: String = "",
    val wardsScore: String = "",
    val wardsControl: String = "",
    val cs: String = "",
    val csPerMinute: String = "",
    val gold: String = "",
    val goldPerMinute: String = "",
    val doubleKills: String = "",
    val tripleKills: String = "",
    val quadraKills: String = "",
    val pentaKills: String = ""
)

@Serializable
data class FormTest(
    val questions: List<Question>,
    val threshold: Int = 2
)

@Serializable
data class Question(
    val question: String,
    val answer: Answer
)

@Serializable
enum class Answer {
    @SerialName("yes") YES,
    @SerialName("no") NO
}

@Serializable
enum class TestResult {
    @SerialName("failed") FAILED,
    @SerialName("passed") PASSED
}