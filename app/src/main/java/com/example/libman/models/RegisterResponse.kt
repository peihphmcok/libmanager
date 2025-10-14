package com.example.libman.models

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("user")
    val user: User? = null
)
