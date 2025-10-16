package com.example.libman.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("library_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("token", token).apply()
    }

    fun getToken(): String? {
        val token = prefs.getString("token", null)
        android.util.Log.d("TokenManager", "getToken: retrieved token: ${if (token != null && token.length > 50) token.substring(0, 50) + "..." else token}")
        return token
    }

    fun saveRole(role: String?) {
        if (role == null) return
        prefs.edit().putString("role", role).apply()
    }

    fun getRole(): String? = prefs.getString("role", null)
    
    fun saveUserId(userId: String?) {
        if (userId == null) return
        prefs.edit().putString("user_id", userId).apply()
    }
    
    fun getUserId(): String? {
        val userId = prefs.getString("user_id", null)
        android.util.Log.d("TokenManager", "getUserId: retrieved user ID: $userId")
        return userId
    }
    
    fun saveUserInfo(user: com.example.libman.models.User?) {
        if (user == null) {
            android.util.Log.d("TokenManager", "saveUserInfo: user is null")
            return
        }
        android.util.Log.d("TokenManager", "saveUserInfo: saving user - id: ${user.id}, name: ${user.fullname ?: user.name}, email: ${user.email}")
        
        // Save all user info in a single transaction
        prefs.edit().apply {
            putString("user_id", user.id)
            putString("user_name", user.fullname ?: user.name)
            putString("user_email", user.email)
            apply()
        }
        
        android.util.Log.d("TokenManager", "saveUserInfo: user info saved successfully")
        
        // Verify the save
        val savedId = prefs.getString("user_id", null)
        android.util.Log.d("TokenManager", "saveUserInfo: verification - saved ID: $savedId")
    }
    
    fun getUserName(): String? = prefs.getString("user_name", null)
    fun getUserEmail(): String? = prefs.getString("user_email", null)
    
    fun clearUserData() {
        android.util.Log.d("TokenManager", "Clearing all user data")
        prefs.edit().apply {
            remove("user_id")
            remove("user_name")
            remove("user_email")
            remove("token")
            remove("role")
            apply()
        }
    }
    
    fun forceClearAndTest() {
        android.util.Log.d("TokenManager", "=== FORCE CLEAR AND TEST ===")
        clearUserData()
        debugUserInfo()
        android.util.Log.d("TokenManager", "=== END FORCE CLEAR ===")
    }
    
    fun isLoggedIn(): Boolean {
        val token = getToken()
        val userId = getUserId()
        android.util.Log.d("TokenManager", "isLoggedIn check - token: ${if (token != null) "present" else "null"}, userId: $userId")
        return !token.isNullOrEmpty() && !userId.isNullOrEmpty()
    }
    
    fun debugUserInfo() {
        android.util.Log.d("TokenManager", "=== DEBUG USER INFO ===")
        android.util.Log.d("TokenManager", "Token: ${if (getToken() != null && getToken()!!.length > 20) "present (${getToken()?.substring(0, 20)}...)" else getToken()}")
        android.util.Log.d("TokenManager", "User ID: ${getUserId()}")
        android.util.Log.d("TokenManager", "User Name: ${getUserName()}")
        android.util.Log.d("TokenManager", "User Email: ${getUserEmail()}")
        android.util.Log.d("TokenManager", "User Role: ${getRole()}")
        android.util.Log.d("TokenManager", "Is Logged In: ${isLoggedIn()}")
        android.util.Log.d("TokenManager", "=== END DEBUG ===")
    }
    
    fun forceLogout() {
        android.util.Log.d("TokenManager", "=== FORCE LOGOUT ===")
        clearUserData()
        android.util.Log.d("TokenManager", "All user data cleared")
    }
}
