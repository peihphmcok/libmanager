package com.example.libman.models

import com.google.gson.annotations.SerializedName

data class ReviewsResponse(
    @SerializedName("reviews")
    val reviews: List<Review>? = null,
    
    @SerializedName("total")
    val total: Int? = null
)
