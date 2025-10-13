package com.example.libman.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("library_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("token", token).apply()
    }

    fun getToken(): String? = prefs.getString("token", null)

    fun saveRole(role: String?) {
        if (role == null) return
        prefs.edit().putString("role", role).apply()
    }

    fun getRole(): String? = prefs.getString("role", null)
}
