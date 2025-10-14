package com.example.libman.models

import com.google.gson.annotations.SerializedName

data class Book(
    @SerializedName("_id")
    val id: String? = null,
    
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("author")
    val author: String? = null,
    
    @SerializedName("category")
    val category: String? = null,
    
    @SerializedName("coverImage")
    val coverImage: String? = null,
    
    @SerializedName("publishedYear")
    val publishedYear: Int? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)