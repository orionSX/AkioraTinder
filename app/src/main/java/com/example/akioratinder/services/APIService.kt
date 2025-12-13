package com.example.akioratinder.services

import android.content.Context
import android.util.Log
import com.example.akioratinder.config.BackendConfig
import com.example.akioratinder.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ApiService private constructor(context: Context) {
    private val client: OkHttpClient
    private val baseUrl: String
        get() = "${BackendConfig.backendUrl}/"

    private var authToken: String? = null
    private val userStore = UserStore(context)

    init {
        client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val builder = original.newBuilder()

                authToken?.let { token ->
                    builder.header("Authorization", "Bearer $token")
                }

                builder.header("Content-Type", "application/json")
                builder.header("Accept", "application/json")
                chain.proceed(builder.build())
            }
            .build()
    }

    companion object {
        @Volatile
        private var INSTANCE: ApiService? = null

        fun getInstance(context: Context): ApiService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiService(context).also { INSTANCE = it }
            }
        }
    }

    fun setAuthToken(token: String) {
        authToken = token
    }

    fun clearAuthToken() {
        authToken = null
    }

    // Аутентификация
    suspend fun login(request: LoginRequest): AuthResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("email", request.email)
            put("password", request.password)
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${baseUrl}users/login")
            .post(requestBody)
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "{}"
                val jsonResponse = JSONObject(responseBody)
                
                // В спецификации не указан точный формат ответа, поэтому используем стандартный подход
                val token = jsonResponse.optString("access_token", "")  // или "token" в зависимости от API
                
                // Получаем данные пользователя
                val user = if (jsonResponse.has("id")) {
                    val parsedUser = parseUserProfile(jsonResponse)
                    userStore.saveUserData(parsedUser)  // Сохраняем данные пользователя
                    parsedUser
                } else {
                    null
                }

                if (token.isNotEmpty()) {
                    authToken = token
                }

                AuthResponse(true, token, user)
            } else {
                AuthResponse(false, "", null)
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Login error: ${e.message}")
            AuthResponse(false, "", null)
        }
    }

    suspend fun register(request: RegisterRequest): AuthResponse = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("name", request.name)
            put("email", request.email)
            put("password", request.password)
            if (request.code != null) {
                put("code", request.code)
            }
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${baseUrl}users/register")
            .post(requestBody)
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "{}"
                val jsonResponse = JSONObject(responseBody)
                
                // В спецификации не указан точный формат ответа, поэтому используем стандартный подход
                val token = jsonResponse.optString("access_token", "")  // или "token" в зависимости от API
                
                // Получаем данные пользователя
                val user = if (jsonResponse.has("id")) {
                    val parsedUser = parseUserProfile(jsonResponse)
                    userStore.saveUserData(parsedUser)  // Сохраняем данные пользователя
                    parsedUser
                } else {
                    null
                }

                if (token.isNotEmpty()) {
                    authToken = token
                }

                AuthResponse(true, token, user)
            } else {
                AuthResponse(false, "", null)
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Register error: ${e.message}")
            AuthResponse(false, "", null)
        }
    }

    // Пользователи
    suspend fun getCurrentUser(): UserProfile? = withContext(Dispatchers.IO) {
        // Возвращаем данные текущего пользователя из UserStore
        return@withContext userStore.getUserData()
    }
    
    suspend fun getUserById(userId: String): UserProfile = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${baseUrl}users/${userId}")
            .get()
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "{}"
                val json = JSONObject(responseBody)
                parseUserProfile(json)
            } else {
                UserProfile()
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Get user by id error: ${e.message}")
            UserProfile()
        }
    }
    
    suspend fun getAllUsers(): List<UserProfile> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${baseUrl}users/")
            .get()
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(responseBody)
                val users = mutableListOf<UserProfile>()
                for (i in 0 until jsonArray.length()) {
                    val userJson = jsonArray.getJSONObject(i)
                    users.add(parseUserProfile(userJson))
                }
                users
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Get all users error: ${e.message}")
            emptyList()
        }
    }
    
    suspend fun updateUser(userId: String, update: UpdateProfileRequest): UserProfile = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            if (update.email != null) put("email", update.email)
            if (update.password != null) put("password", update.password)
            if (update.name != null) put("name", update.name)
            if (update.age != null) put("age", update.age)
            if (update.gender != null) put("gender", update.gender.toString().lowercase())
            if (update.discord != null) put("discord", update.discord)
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${baseUrl}users/${userId}")
            .patch(requestBody)  // Изменено на PATCH, как указано в OpenAPI спецификации
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "{}"
                val json = JSONObject(responseBody)
                parseUserProfile(json)
            } else {
                UserProfile()
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Update user error: ${e.message}")
            UserProfile()
        }
    }

    // Формы (Player Profiles)
    suspend fun getForms(): List<PlayerProfile> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${baseUrl}forms")
            .get()
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(responseBody)
                parsePlayerProfiles(jsonArray)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Get forms error: ${e.message}")
            emptyList()
        }
    }

    suspend fun getFormById(id: String): PlayerProfile? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${baseUrl}forms/$id")
            .get()
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "{}"
                val json = JSONObject(responseBody)
                parsePlayerProfile(json)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Get form by id error: ${e.message}")
            null
        }
    }

    suspend fun createForm(requestData: CreateFormRequest): PlayerProfile? = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            if (requestData.description != null) put("description", requestData.description)
            put("account", JSONObject().apply {
                put("name", requestData.account.name)
                put("server", requestData.account.server)
                put("tag", requestData.account.tag)
            })
            put("roles", JSONArray(requestData.roles.map { it.toString().lowercase() }))
            put("roles_looking_for", JSONArray(requestData.rolesLookingFor.map { it.toString().lowercase() }))
            put("person_data", JSONObject().apply {
                if (requestData.personData.minAge != null) put("min_age", requestData.personData.minAge)
                if (requestData.personData.maxAge != null) put("max_age", requestData.personData.maxAge)
                if (requestData.personData.gender != null) put("gender", requestData.personData.gender.toString().lowercase())
                put("voice", requestData.personData.voice)
            })
            put("creator_id", requestData.creatorId)
            put("game_types", JSONArray(requestData.gameTypes.map { it.toString().lowercase() }))
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${baseUrl}forms")
            .post(requestBody)
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "{}"
                val json = JSONObject(responseBody)
                parsePlayerProfile(json)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Create form error: ${e.message}")
            null
        }
    }

    suspend fun updateForm(id: String, update: UpdateFormRequest): PlayerProfile? = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            if (update.description != null) put("description", update.description)
            if (update.account != null) {
                put("account", JSONObject().apply {
                    put("name", update.account.name)
                    put("server", update.account.server)
                    put("tag", update.account.tag)
                })
            }
            if (update.roles != null) {
                put("roles", JSONArray(update.roles.map { it.toString().lowercase() }))
            }
            if (update.rolesLookingFor != null) {
                put("roles_looking_for", JSONArray(update.rolesLookingFor.map { it.toString().lowercase() }))
            }
            if (update.personData != null) {
                put("person_data", JSONObject().apply {
                    if (update.personData.minAge != null) put("min_age", update.personData.minAge)
                    if (update.personData.maxAge != null) put("max_age", update.personData.maxAge)
                    if (update.personData.gender != null) put("gender", update.personData.gender.toString().lowercase())
                    put("voice", update.personData.voice)
                })
            }
            if (update.gameTypes != null) {
                put("game_types", JSONArray(update.gameTypes.map { it.toString().lowercase() }))
            }
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${baseUrl}forms/$id")
            .put(requestBody)
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "{}"
                val json = JSONObject(responseBody)
                parsePlayerProfile(json)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Update form error: ${e.message}")
            null
        }
    }

    suspend fun deleteForm(id: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${baseUrl}forms/$id")
            .delete()
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ApiService", "Delete form error: ${e.message}")
            false
        }
    }

    // Лайки/Дизлайки
    suspend fun likeForm(formId: String, userId: String? = null): Boolean = withContext(Dispatchers.IO) {
        val actualUserId = userId ?: userStore.getUserId() ?: return@withContext false
        val request = Request.Builder()
            .url("${baseUrl}forms/$formId/like?user_id=$actualUserId")
            .post(RequestBody.create(null, ""))
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ApiService", "Like form error: ${e.message}")
            false
        }
    }

    suspend fun dislikeForm(formId: String, userId: String? = null): Boolean = withContext(Dispatchers.IO) {
        val actualUserId = userId ?: userStore.getUserId() ?: return@withContext false
        val request = Request.Builder()
            .url("${baseUrl}forms/$formId/dislike?user_id=$actualUserId")
            .post(RequestBody.create(null, ""))
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ApiService", "Dislike form error: ${e.message}")
            false
        }
    }
    
    suspend fun activateForm(formId: String, userId: String? = null): Boolean = withContext(Dispatchers.IO) {
        val actualUserId = userId ?: userStore.getUserId() ?: return@withContext false
        val request = Request.Builder()
            .url("${baseUrl}forms/activate/$formId?user_id=$actualUserId")
            .post(RequestBody.create(null, ""))
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ApiService", "Activate form error: ${e.message}")
            false
        }
    }
    
    suspend fun deleteFormById(formId: String, userId: String? = null): Boolean = withContext(Dispatchers.IO) {
        val actualUserId = userId ?: userStore.getUserId() ?: return@withContext false
        val request = Request.Builder()
            .url("${baseUrl}forms/delete/$formId?user_id=$actualUserId")
            .post(RequestBody.create(null, ""))
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ApiService", "Delete form by id error: ${e.message}")
            false
        }
    }

    // Чаты
    suspend fun getChats(): List<Chat> = withContext(Dispatchers.IO) {
        val userId = userStore.getUserId() ?: return@withContext emptyList()
        val request = Request.Builder()
            .url("${baseUrl}chats?user_id=$userId")
            .get()
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(responseBody)
                parseChats(jsonArray)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Get chats error: ${e.message}")
            emptyList()
        }
    }

    suspend fun getChatById(chatId: String): Chat? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${baseUrl}chats/$chatId")
            .get()
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "{}"
                val json = JSONObject(responseBody)
                parseChat(json)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Get chat by id error: ${e.message}")
            null
        }
    }

    suspend fun getChatBetweenUsers(user1Id: String, user2Id: String): Chat? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${baseUrl}chats/between/$user1Id/$user2Id")
            .get()
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "{}"
                val json = JSONObject(responseBody)
                parseChat(json)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Get chat between users error: ${e.message}")
            null
        }
    }

    suspend fun ignoreChat(chatId: String): Boolean = withContext(Dispatchers.IO) {
        val userId = userStore.getUserId() ?: return@withContext false
        val request = Request.Builder()
            .url("${baseUrl}chats/$chatId/ignore?user_id=$userId")
            .post(RequestBody.create(null, ""))
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ApiService", "Ignore chat error: ${e.message}")
            false
        }
    }

    suspend fun unignoreChat(chatId: String): Boolean = withContext(Dispatchers.IO) {
        val userId = userStore.getUserId() ?: return@withContext false
        val request = Request.Builder()
            .url("${baseUrl}chats/$chatId/unignore?user_id=$userId")
            .post(RequestBody.create(null, ""))
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ApiService", "Unignore chat error: ${e.message}")
            false
        }
    }

    suspend fun getMessages(chatId: String, skip: Int = 0, limit: Int = 50, before: String? = null): List<ChatMessage> = withContext(Dispatchers.IO) {
        var url = "${baseUrl}chats/$chatId/messages?skip=$skip&limit=$limit"
        if (before != null) {
            url += "&before=$before"
        }
        
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(responseBody)
                parseMessages(jsonArray)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Get messages error: ${e.message}")
            emptyList()
        }
    }

    suspend fun markMessageAsRead(messageId: String): Boolean = withContext(Dispatchers.IO) {
        val userId = userStore.getUserId() ?: return@withContext false
        val request = Request.Builder()
            .url("${baseUrl}chats/messages/$messageId/read?user_id=$userId")
            .put(RequestBody.create(null, ""))
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ApiService", "Mark message as read error: ${e.message}")
            false
        }
    }

    suspend fun deleteMessage(messageId: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${baseUrl}chats/messages/$messageId")
            .delete()
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ApiService", "Delete message error: ${e.message}")
            false
        }
    }
    
    // Рекомендации
    suspend fun getRecommendedForms(userId: String? = null): List<PlayerProfile> = withContext(Dispatchers.IO) {
        val actualUserId = userId ?: userStore.getUserId() ?: return@withContext emptyList()
        val request = Request.Builder()
            .url("${baseUrl}forms/recommendations/$actualUserId")
            .get()
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "[]"
                val jsonArray = JSONArray(responseBody)
                val forms = mutableListOf<PlayerProfile>()
                for (i in 0 until jsonArray.length()) {
                    val formJson = jsonArray.getJSONObject(i)
                    forms.add(parsePlayerProfile(formJson))
                }
                forms
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Get recommended forms error: ${e.message}")
            emptyList()
        }
    }

    suspend fun sendMessage(chatId: String, text: String): ChatMessage? = withContext(Dispatchers.IO) {
        val userId = userStore.getUserId() ?: return@withContext null
        val json = JSONObject().apply {
            put("text", text)
            put("creator_id", userId)  // Добавляем ID текущего пользователя как создателя сообщения
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${baseUrl}chats/$chatId/messages")
            .post(requestBody)
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "{}"
                val json = JSONObject(responseBody)
                parseMessage(json)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Send message error: ${e.message}")
            null
        }
    }

    suspend fun createChat(user2Id: String): Chat? = withContext(Dispatchers.IO) {
        val userId = userStore.getUserId() ?: return@withContext null
        val json = JSONObject().apply {
            put("user_1", userId)
            put("user_2", user2Id)
        }

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${baseUrl}chats")
            .post(requestBody)
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "{}"
                val json = JSONObject(responseBody)
                parseChat(json)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Create chat error: ${e.message}")
            null
        }
    }

    // Парсеры данных
    private fun parseUserProfile(json: JSONObject): UserProfile {
        return UserProfile(
            id = json.optString("_id", ""),
            name = json.optString("name", ""),
            email = json.optString("email", ""),
            age = json.optInt("age").takeIf { it > 0 },
            gender = when (json.optString("gender", "").lowercase()) {
                "male" -> Gender.MALE
                "female" -> Gender.FEMALE
                "any" -> Gender.ANY
                else -> null
            },
            discord = json.optString("discord").takeIf { it.isNotEmpty() },
            role = json.optString("role", "user")
        )
    }

    private fun parsePlayerProfiles(jsonArray: JSONArray): List<PlayerProfile> {
        val profiles = mutableListOf<PlayerProfile>()
        for (i in 0 until jsonArray.length()) {
            val profile = parsePlayerProfile(jsonArray.getJSONObject(i))
            if (profile != null) {
                profiles.add(profile)
            }
        }
        return profiles
    }

    private fun parsePlayerProfile(json: JSONObject): PlayerProfile? {
        return try {
            PlayerProfile(
                id = json.optString("_id", ""),
                creatorId = json.optString("creator_id", ""),
                account = parseAccount(json.getJSONObject("account")), // account на верхнем уровне
                description = json.optString("description", ""),
                gameData = parseGameData(json.getJSONObject("game_data")),
                personData = parsePersonData(json.getJSONObject("person_data")),
                userData = parseUserData(json.getJSONObject("user_data")),
                createdAt = json.optString("created_at", ""),
                deleted = json.optBoolean("deleted", false),
                active = json.optBoolean("active", false),
                likedBy = parseStringList(json.optJSONArray("liked_by")),
                dislikedBy = parseStringList(json.optJSONArray("disliked_by")),
                formTest = parseFormTest(json.optJSONObject("form_test")),
                testResults = parseTestResults(json.optJSONObject("test_results"))
            )
        } catch (e: Exception) {
            Log.e("ApiService", "Parse player profile error: ${e.message}")
            null
        }
    }

    private fun parseAccount(json: JSONObject): Account {
        return Account(
            name = json.optString("name", ""),
            server = json.optString("server", ""),
            tag = json.optString("tag", "")
        )
    }

    private fun parseGameData(json: JSONObject): GameData {
        return GameData(
            roles = parseRoleList(json.optJSONArray("roles")),
            rolesLookingFor = parseRoleList(json.optJSONArray("roles_looking_for")),
            stats = parseSummonerStats(json.optJSONObject("stats")),
            canPlayWith = parseCanPlayWith(json.optJSONObject("can_play_with")),
            gameTypes = parseGameTypeList(json.optJSONArray("game_types"))
        )
    }

    private fun parseSummonerStats(json: JSONObject?): SummonerStats {
        if (json == null) return SummonerStats()

        return SummonerStats(
            soloQueue = parseRankedStats(json.optJSONObject("solo_queue")),
            flexQueue = parseRankedStats(json.optJSONObject("flex_queue")),
            championStats = parseChampionStatsList(json.optJSONArray("champion_stats"))
        )
    }

    private fun parseRankedStats(json: JSONObject?): RankedStats? {
        if (json == null) return null

        return RankedStats(
            currentRank = json.optString("current_rank", ""),
            currentLp = json.optString("current_lp", ""),
            winLoss = json.optString("win_loss", ""),
            winRate = json.optString("win_rate", ""),
            bestRank = json.optString("best_rank", ""),
            bestLp = json.optString("best_lp", ""),
            iconId = json.optInt("icon_id").takeIf { it > 0 }
        )
    }

    private fun parseChampionStatsList(jsonArray: JSONArray?): List<ChampionStats> {
        if (jsonArray == null) return emptyList()

        val stats = mutableListOf<ChampionStats>()
        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            stats.add(ChampionStats(
                position = json.optString("position", ""),
                champion = json.optString("champion", ""),
                wins = json.optString("wins", ""),
                losses = json.optString("losses", ""),
                winRate = json.optString("win_rate", ""),
                kdaRatio = json.optString("kda_ratio", ""),
                kda = json.optString("kda", ""),
                laning = json.optString("laning", ""),
                damagePerMinute = json.optString("damage_per_minute", ""),
                damageShareRatio = json.optString("damage_share_ratio", ""),
                wardsScore = json.optString("wards_score", ""),
                wardsControl = json.optString("wards_control", ""),
                cs = json.optString("cs", ""),
                csPerMinute = json.optString("cs_per_minute", ""),
                gold = json.optString("gold", ""),
                goldPerMinute = json.optString("gold_per_minute", ""),
                doubleKills = json.optString("double_kills", ""),
                tripleKills = json.optString("triple_kills", ""),
                quadraKills = json.optString("quadra_kills", ""),
                pentaKills = json.optString("penta_kills", "")
            ))
        }
        return stats
    }

    private fun parseCanPlayWith(json: JSONObject?): Map<String, Any>? {
        if (json == null) return null

        val map = mutableMapOf<String, Any>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = json.get(key)
        }
        return map
    }

    private fun parseFormTest(json: JSONObject?): FormTest? {
        if (json == null) return null

        return try {
            FormTest(
                questions = parseQuestions(json.getJSONArray("questions")),
                threshold = json.optInt("threshold", 2)
            )
        } catch (e: Exception) {
            Log.e("ApiService", "Parse form test error: ${e.message}")
            null
        }
    }

    private fun parseQuestions(jsonArray: JSONArray): List<Question> {
        val questions = mutableListOf<Question>()
        for (i in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(i)
            questions.add(Question(
                question = json.optString("question", ""),
                answer = when (json.optString("answer", "").lowercase()) {
                    "yes" -> Answer.YES
                    "no" -> Answer.NO
                    else -> Answer.YES // default
                }
            ))
        }
        return questions
    }

    private fun parseTestResults(json: JSONObject?): Map<String, TestResult> {
        if (json == null) return emptyMap()

        val map = mutableMapOf<String, TestResult>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.getString(key)
            map[key] = when (value.lowercase()) {
                "passed" -> TestResult.PASSED
                "failed" -> TestResult.FAILED
                else -> TestResult.FAILED // default
            }
        }
        return map
    }


    private fun parsePersonData(json: JSONObject): PersonData {
        return PersonData(
            minAge = json.optInt("min_age").takeIf { it > 0 },
            maxAge = json.optInt("max_age").takeIf { it > 0 },
            gender = when (json.optString("gender", "").lowercase()) {
                "male" -> Gender.MALE
                "female" -> Gender.FEMALE
                "any" -> Gender.ANY
                else -> null
            },
            voice = json.optBoolean("voice", false)
        )
    }

    private fun parseUserData(json: JSONObject): UserData {
        return UserData(
            age = json.optInt("age").takeIf { it > 0 },
            gender = when (json.optString("gender", "").lowercase()) {
                "male" -> Gender.MALE
                "female" -> Gender.FEMALE
                "any" -> Gender.ANY
                else -> null
            },
            name = json.optString("name", ""),
            discord = json.optString("discord").takeIf { it.isNotEmpty() }
        )
    }

    private fun parseChats(jsonArray: JSONArray): List<Chat> {
        val chats = mutableListOf<Chat>()
        for (i in 0 until jsonArray.length()) {
            val chat = parseChat(jsonArray.getJSONObject(i))
            if (chat != null) {
                chats.add(chat)
            }
        }
        return chats
    }

    private fun parseChat(json: JSONObject): Chat? {
        return try {
            Chat(
                id = json.optString("_id", ""),
                user1 = json.optString("user_1", ""),
                user2 = json.optString("user_2", ""),
                ignoredBy = parseStringList(json.optJSONArray("ignored_by")),
                createdAt = json.optString("created_at", "")
            )
        } catch (e: Exception) {
            Log.e("ApiService", "Parse chat error: ${e.message}")
            null
        }
    }

    private fun parseMessages(jsonArray: JSONArray): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        for (i in 0 until jsonArray.length()) {
            val message = parseMessage(jsonArray.getJSONObject(i))
            if (message != null) {
                messages.add(message)
            }
        }
        return messages
    }

    private fun parseMessage(json: JSONObject): ChatMessage? {
        return try {
            ChatMessage(
                id = json.optString("_id", ""),
                creatorId = json.optString("creator_id", ""),
                text = json.optString("text", ""),
                timestamp = json.optString("timestamp", ""),
                status = when (json.optString("status", "").lowercase()) {
                    "delivered" -> MessageStatus.DELIVERED
                    "read" -> MessageStatus.READ
                    else -> MessageStatus.SENT
                },
                chatId = json.optString("chat_id", "")
            )
        } catch (e: Exception) {
            Log.e("ApiService", "Parse message error: ${e.message}")
            null
        }
    }

    // Вспомогательные парсеры
    private fun parseRoleList(jsonArray: JSONArray?): List<Role> {
        if (jsonArray == null) return emptyList()
        val roles = mutableListOf<Role>()
        for (i in 0 until jsonArray.length()) {
            when (jsonArray.getString(i).lowercase()) {
                "top" -> roles.add(Role.TOP)
                "jg" -> roles.add(Role.JG)
                "mid" -> roles.add(Role.MID)
                "adc" -> roles.add(Role.ADC)
                "sup" -> roles.add(Role.SUP)
                "any" -> roles.add(Role.ANY)
            }
        }
        return roles
    }

    private fun parseGameTypeList(jsonArray: JSONArray?): List<GameType> {
        if (jsonArray == null) return emptyList()
        val gameTypes = mutableListOf<GameType>()
        for (i in 0 until jsonArray.length()) {
            when (jsonArray.getString(i).lowercase()) {
                "normal" -> gameTypes.add(GameType.NORMAL)
                "aram" -> gameTypes.add(GameType.ARAM)
                "arena" -> gameTypes.add(GameType.ARENA)
                "soloq" -> gameTypes.add(GameType.SOLOQ)
                "flex" -> gameTypes.add(GameType.FLEX)
                "any" -> gameTypes.add(GameType.ANY)
            }
        }
        return gameTypes
    }

    private fun parseStringList(jsonArray: JSONArray?): List<String> {
        if (jsonArray == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        return list
    }
}