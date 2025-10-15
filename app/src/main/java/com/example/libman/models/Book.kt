package com.example.libman.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    @SerializedName("_id")
    val id: String? = null,
    
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("author")
    val author: String? = null,
    
    @SerializedName("category")
    val category: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("isbn")
    val isbn: String? = null,
    
    @SerializedName("coverImage")
    val coverImage: String? = null,
    
    @SerializedName("publishedYear")
    val publishedYear: Int? = null,
    
    @SerializedName("available")
    val available: Boolean? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) : Parcelable