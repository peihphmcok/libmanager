package com.example.libman.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    @SerializedName("_id")
    val id: String? = null,
    
    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("fullname")
    val fullname: String? = null,
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("email")
    val email: String? = null,
    
    @SerializedName("role")
    val role: String? = null, // "admin" hoặc "user"
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) : Parcelable