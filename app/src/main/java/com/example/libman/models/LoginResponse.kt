package com.example.libman.models
data class LoginResponse(
    val message: String,
    val token: String?,
    val user: User? // Giả sử User model đã có
)