package com.example.libman.models

data class Book(
    val id: Int,
    val title: String,
    val author: String,
    val description: String,
    val imageUrl: String?,
    val available: Boolean
)