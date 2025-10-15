package com.example.libman.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Author(
    @SerializedName("_id")
    val id: String? = null,
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("bio")
    val bio: String? = null,
    
    @SerializedName("nationality")
    val nationality: String? = null,
    
    @SerializedName("birthYear")
    val birthYear: Int? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) : Parcelable