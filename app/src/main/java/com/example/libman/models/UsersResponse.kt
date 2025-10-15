package com.example.libman.models

import com.google.gson.annotations.SerializedName

data class UsersResponse(
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("total")
    val total: Int? = null,
    
    @SerializedName("currentPage")
    val currentPage: Int? = null,
    
    @SerializedName("totalPages")
    val totalPages: Int? = null,
    
    @SerializedName("users")
    val users: List<User>? = null
)
