package com.example.libman.models

import com.google.gson.annotations.SerializedName

data class Author(
    @SerializedName("_id") val id: String?,
    val name: String,
    val bio: String?,
    val nationality: String?,
    val birthyear: Int?
)