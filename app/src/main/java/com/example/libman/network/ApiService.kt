package com.example.libman.network

import com.example.libman.models.Book
import com.example.libman.models.Author
import com.example.libman.models.LoginRequest
import com.example.libman.models.LoginResponse
import com.example.libman.models.RegisterRequest
import com.example.libman.models.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
interface ApiService {
    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("users/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @GET("books")
    suspend fun getBooks(@Query("search") search: String? = null): List<Book>

    @POST("books")
    suspend fun addBook(@Body book: Book): Book

    // Authors
    @GET("authors")
    suspend fun getAuthors(@Query("search") search: String? = null): List<Author>
}