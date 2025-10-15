package com.example.libman.models

import com.google.gson.annotations.SerializedName

data class AuthorsResponse(
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("totalAuthors")
    val totalAuthors: Int? = null,
    
    @SerializedName("currentPage")
    val currentPage: Int? = null,
    
    @SerializedName("authors")
    val authors: List<Author>? = null
)
