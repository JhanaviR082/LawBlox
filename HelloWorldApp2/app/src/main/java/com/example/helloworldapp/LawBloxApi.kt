package com.example.helloworldapp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


interface LawBloxApi {

    // Signup endpoint
    @POST("/api/auth/signup")
    fun signup(@Body request: SignupRequest): Call<AuthResponse>

    // Login endpoint
    @POST("/api/auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    // Chat endpoint (requires JWT token in Authorization header)
    @POST("/api/chat/message")
    fun sendMessage(
        @Body request: ChatRequest,
        @Header("Authorization") authHeader: String // "Bearer <token>"
    ): Call<Map<String, Any>>
}

