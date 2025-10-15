package com.example.libman.models

import com.google.gson.annotations.SerializedName

data class LoansResponse(
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("loans")
    val loans: List<Loan>? = null
)
