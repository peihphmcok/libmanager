package com.example.libman.network

import com.example.libman.models.Book
import com.example.libman.models.Author
import com.example.libman.models.Loan
import com.example.libman.models.User
import com.example.libman.models.LoginRequest
import com.example.libman.models.LoginResponse
import com.example.libman.models.RegisterRequest
import com.example.libman.models.RegisterResponse
import com.example.libman.models.ApiResponse
import com.example.libman.models.Review
import com.example.libman.models.BorrowRequest
import com.example.libman.models.BooksResponse
import com.example.libman.models.AuthorsResponse
import com.example.libman.models.UsersResponse
import com.example.libman.models.LoansResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Query
import retrofit2.http.Path
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiService {
    // Authentication
    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("users/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    // Books
    @GET("books")
    suspend fun getBooks(
        @Query("search") search: String? = null,
        @Query("limit") limit: Int = 100
    ): BooksResponse

    @POST("books")
    suspend fun addBook(@Body book: Book): Book

    @GET("books/{id}")
    suspend fun getBook(@Path("id") id: String): Book

    @PUT("books/{id}")
    suspend fun updateBook(@Path("id") id: String, @Body book: Book): Book

    @DELETE("books/{id}")
    suspend fun deleteBook(@Path("id") id: String): Unit

    // Book cover upload
    @Multipart
    @POST("books/{id}/upload-cover")
    suspend fun uploadBookCover(
        @Path("id") id: String,
        @Part cover: MultipartBody.Part
    ): ApiResponse

    // Authors
    @GET("authors")
    suspend fun getAuthors(
        @Query("search") search: String? = null,
        @Query("limit") limit: Int = 100
    ): AuthorsResponse

    @POST("authors")
    suspend fun addAuthor(@Body author: Author): Author

    @GET("authors/{id}")
    suspend fun getAuthor(@Path("id") id: String): Author

    @PUT("authors/{id}")
    suspend fun updateAuthor(@Path("id") id: String, @Body author: Author): Author

    @DELETE("authors/{id}")
    suspend fun deleteAuthor(@Path("id") id: String): Unit

    // Loans
    @GET("loans")
    suspend fun getLoans(@Query("search") search: String? = null): LoansResponse

    // Create loan (borrow)
    @POST("loans/borrow")
    suspend fun addLoan(@Body borrowRequest: BorrowRequest): Loan

    // Mark as returned
    @PUT("loans/{id}/return")
    suspend fun updateLoan(@Path("id") id: String, @Body body: Map<String, @JvmSuppressWildcards Any> = emptyMap()): ApiResponse

    // Get user loans
    @GET("loans/user/{userId}")
    suspend fun getUserLoans(@Path("userId") userId: String): LoansResponse

    // Users
    @GET("users")
    suspend fun getUsers(@Query("search") search: String? = null): UsersResponse

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): User

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: User): User

    // Upload user profile picture
    @Multipart
    @POST("users/{id}/upload-profile-picture")
    suspend fun uploadUserProfilePicture(
        @Path("id") id: String,
        @Part profile: MultipartBody.Part
    ): ApiResponse

    // Reviews for a book
    @GET("books/{id}/reviews")
    suspend fun getBookReviews(@Path("id") bookId: String): List<Review>

    @POST("books/{id}/reviews")
    suspend fun addBookReview(@Path("id") bookId: String, @Body review: Review): Review

    @PUT("books/{id}/reviews/{reviewId}")
    suspend fun updateBookReview(
        @Path("id") bookId: String,
        @Path("reviewId") reviewId: String,
        @Body review: Review
    ): Review

    @DELETE("books/{id}/reviews/{reviewId}")
    suspend fun deleteBookReview(
        @Path("id") bookId: String,
        @Path("reviewId") reviewId: String
    ): ApiResponse

    data class ChangePasswordRequest(
        val userId: String,
        val oldPassword: String,
        val newPassword: String
    )

    @POST("users/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    data class UpdateUserRequest(
        val userId: String,
        val fullname: String?,
        val email: String?
    )

    @PUT("users/update")
    suspend fun updateUser(@Body request: UpdateUserRequest): Response<User>
}