package com.example.libman.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class BorrowRequest(
    @SerializedName("bookId")
    val bookId: String,
    
    @SerializedName("dueDate")
    val dueDate: Date
)
