// AuthService.kt
package com.example.libman.network

import com.example.libman.models.LoginRequest
import com.example.libman.models.LoginResponse
import com.example.libman.models.RegisterRequest
import com.example.libman.models.RegisterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("users/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("users/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}
