package com.example.libman.models

import com.google.gson.annotations.SerializedName

data class ReviewResponse(
    @SerializedName("review")
    val review: Review? = null
)
