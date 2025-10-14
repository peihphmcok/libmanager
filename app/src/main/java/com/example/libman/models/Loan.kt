package com.example.libman.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Loan(
    @SerializedName("_id")
    val id: String? = null,
    
    @SerializedName("book")
    val book: Book? = null,
    
    @SerializedName("user")
    val user: User? = null,
    
    @SerializedName("issueDate")
    val borrowDate: Date? = null,
    
    @SerializedName("isReturned")
    val isReturned: Boolean? = null,
    
    @SerializedName("fineAmount")
    val fineAmount: Double? = null,
    
    @SerializedName("dueDate")
    val dueDate: Date? = null,
    
    @SerializedName("returnDate")
    val returnDate: Date? = null,
    
    @SerializedName("status")
    val status: String? = null, // "borrowed", "returned", "overdue"
    
    @SerializedName("createdAt")
    val createdAt: Date? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: Date? = null
)