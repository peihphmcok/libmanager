package com.example.libman.models

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("fullname")
    val fullname: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)