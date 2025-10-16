package com.example.libman.models

import com.google.gson.annotations.SerializedName

data class BookResponse(
    @SerializedName("book")
    val book: Book? = null
)
