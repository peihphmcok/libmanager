package com.example.libman.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Loan(
    @SerializedName("_id")
    val id: String? = null,
    
    @SerializedName("book")
    val book: Book? = null,
    
    @SerializedName("user")
    val user: User? = null,
    
    @SerializedName("issueDate")
    val borrowDate: String? = null,
    
    @SerializedName("isReturned")
    val isReturned: Boolean? = null,
    
    @SerializedName("fineAmount")
    val fineAmount: Double? = null,
    
    @SerializedName("dueDate")
    val dueDate: String? = null,
    
    @SerializedName("returnDate")
    val returnDate: String? = null,
    
    @SerializedName("status")
    val status: String? = null, // "borrowed", "returned", "overdue"
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) : Parcelable