package com.example.libman.network

import com.example.libman.models.Book
import retrofit2.Call
import retrofit2.http.GET

interface BookService {
    @GET("books")
    fun getBooks(): Call<List<Book>>
}
