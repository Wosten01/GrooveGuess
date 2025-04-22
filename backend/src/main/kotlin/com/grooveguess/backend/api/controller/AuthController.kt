package com.grooveguess.backend.api.controller

import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import com.grooveguess.backend.security.JwtUtil
import com.grooveguess.backend.domain.dto.LoginRequest
import com.grooveguess.backend.domain.dto.AuthResponse
import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.repository.UserRepository


@RestController
@RequestMapping("/api/auth")
class AuthController(
        private val jwtUtil: JwtUtil,
        private val userRepository: UserRepository,
        private val passwordEncoder: PasswordEncoder,
) {

    @PostMapping("/login")
    fun login(@RequestBody user: User): ResponseEntity<String> {
        val token = jwtUtil.generateToken(user)
        return ResponseEntity.ok(token)
    }

    @PostMapping("/register")
    fun register(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        if (userRepository.findByEmail(request.email) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(AuthResponse(token = null, message = "User already exists"))
        }
        val user = User(
            username = request.username,
            username = request.email,
            password = passwordEncoder.encode(request.password)
        )
        userRepository.save(user)
        val token = jwtUtil.generateToken(user)
        return ResponseEntity.ok(AuthResponse(token = token, message = "Registration successful"))
    }

    @GetMapping("/validate")
    fun validate(@RequestHeader("Authorization") authHeader: String?): ResponseEntity<Boolean> {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(false)
        }
        val token = authHeader.substringAfter("Bearer ").trim()
        val isValid = jwtUtil.validateToken(token)
        return ResponseEntity.ok(isValid)
    }
}