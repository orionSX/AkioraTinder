package com.example.akioratinder.data
import android.content.Context


class Preferences(private val ctx: Context) {
    private val prefs = ctx.getSharedPreferences("akiora_prefs", Context.MODE_PRIVATE)


    fun saveUser(email: String, password:String,server: String, role: String, rank: String) {
        prefs.edit()
            .putString("email", email)
            .putString("password",password)
            .putString("server", server)
            .putString("role", role)
            .putString("rank", rank)
            .apply()
    }


    fun getUser(): Map<String, String?> = mapOf(
        "email" to prefs.getString("email", null),
        "password" to prefs.getString("password", null),
        "server" to prefs.getString("server", null),
        "role" to prefs.getString("role", null),
        "rank" to prefs.getString("rank", null)
    )


    fun clear() { prefs.edit().clear().apply() }
}
