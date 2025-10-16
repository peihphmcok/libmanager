package com.example.libman.controller.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.libman.R
import com.example.libman.models.LoginRequest
import com.example.libman.models.LoginResponse
import com.example.libman.network.ApiClient
import com.example.libman.network.ApiService
import com.example.libman.utils.TokenManager
import com.example.libman.controller.home.HomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoRegister: TextView
    private lateinit var apiService: ApiService
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etLoginEmail)
        etPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoRegister = findViewById(R.id.tvGoRegister)

        apiService = ApiClient.getRetrofit(this).create(ApiService::class.java)
        tokenManager = TokenManager(this)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        apiService.login(LoginRequest(email, pass))
                    }
                    if (!response.token.isNullOrEmpty()) {
                        android.util.Log.d("LoginActivity", "Login successful - saving user info")
                        android.util.Log.d("LoginActivity", "User: ${response.user}")
                        android.util.Log.d("LoginActivity", "User ID: ${response.user?.id}")
                        android.util.Log.d("LoginActivity", "User Name: ${response.user?.fullname ?: response.user?.name}")
                        
                        tokenManager.saveToken(response.token)
                        tokenManager.saveRole(response.user?.role)
                        tokenManager.saveUserInfo(response.user)
                        
                        // Verify that user info was saved
                        val savedUserId = tokenManager.getUserId()
                        android.util.Log.d("LoginActivity", "Saved user ID: $savedUserId")
                        
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Login failed: Invalid response", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}
