package com.grooveguess.backend.api.controller

import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import com.grooveguess.backend.security.JwtUtil
import com.grooveguess.backend.domain.dto.LoginRequest
import com.grooveguess.backend.domain.dto.AuthResponse
import com.grooveguess.backend.domain.model.User


@RestController
@RequestMapping("/api/auth")
class AuthController(private val jwtUtil: JwtUtil) {

    @PostMapping("/login")
    fun login(@RequestBody user: User): ResponseEntity<String> {
        val token = jwtUtil.generateToken(user)
        return ResponseEntity.ok(token)
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