package com.grooveguess.backend.domain.dto

data class RegisterDTO(
    val email: String,
    val username: String,
    val password: String
)

data class LoginDTO(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val message: Message
)