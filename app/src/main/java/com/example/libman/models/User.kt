package com.example.libman.models

data class User(
    val _id: String?,
    val username: String?,
    val email: String?,
    val role: String?  // “admin” hoặc “user”
)