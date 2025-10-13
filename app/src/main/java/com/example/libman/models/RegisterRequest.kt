package com.example.libman.models

data class RegisterRequest(
    val fullname: String,
    val email: String,
    val password: String
)