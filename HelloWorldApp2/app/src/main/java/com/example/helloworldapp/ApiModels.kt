package com.example.helloworldapp
// Signup request and response
data class SignupRequest(
    val email: String,
    val password: String,
    val firstName: String
)

data class AuthResponse(
    val token: String,
    val message: String
)

// Login request
data class LoginRequest(
    val email: String,
    val password: String
)

// Chat request
data class ChatRequest(
    val message: String
)

