package com.example.libman.models

import com.google.gson.annotations.SerializedName

data class Review(
    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("book")
    val book: Book? = null,

    @SerializedName("rating")
    val rating: Int? = null,

    @SerializedName("comment")
    val comment: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)


