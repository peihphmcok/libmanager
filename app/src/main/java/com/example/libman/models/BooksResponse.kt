package com.example.libman.models

import com.google.gson.annotations.SerializedName

data class BooksResponse(
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("total")
    val total: Int? = null,
    
    @SerializedName("currentPage")
    val currentPage: Int? = null,
    
    @SerializedName("books")
    val books: List<Book>? = null
)
