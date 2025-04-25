package com.grooveguess.backend.api.controller

import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.beans.factory.annotation.Value
import com.grooveguess.backend.domain.dto.AuthResponse
import com.grooveguess.backend.domain.dto.RegisterDTO
import com.grooveguess.backend.domain.dto.LoginDTO
import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.repository.UserRepository
import com.grooveguess.backend.domain.enum.Role
import com.grooveguess.backend.service.UserService
import com.grooveguess.backend.domain.dto.Message
import com.grooveguess.backend.util.JwtUtil
import com.grooveguess.backend.util.CookieUtils
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.Jwts
import java.util.Date
import java.nio.charset.StandardCharsets
import javax.crypto.spec.SecretKeySpec
import java.util.Base64
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import io.jsonwebtoken.JwtException

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val cookieUtils: CookieUtils,
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginDTO, response: HttpServletResponse): ResponseEntity<Message> {
        if (request.email.isBlank() || !request.email.contains("@")) {
            return ResponseEntity.badRequest().body(Message("Invalid email format"))
        }
        if (request.password.isBlank()) {
            return ResponseEntity.badRequest().body(Message("Password cannot be blank"))
        }
        val user = userService.findByEmail(request.email)
            ?: return ResponseEntity.status(401).body(Message("User not found"))

        if (!userService.comparePassword(user, request.password)){
            return ResponseEntity.status(401).body(Message("Invalid credentials"))
        }

        val token = jwtUtil.generateToken(user)
        cookieUtils.addJwtCookie(response, token)

        return ResponseEntity.ok(Message("Successfully login"))
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterDTO): ResponseEntity<Message> {
        println(request)
        if (request.email.isBlank() || !request.email.contains("@")) {
            return ResponseEntity.badRequest().body(Message("Invalid email format"))
        }
        if (request.password.isBlank()) {
            return ResponseEntity.badRequest().body(Message("Password cannot be blank"))
        }
        if (request.username.isBlank()) {
            return ResponseEntity.badRequest().body(Message("Username cannot be blank"))
        }
        if (userService.findByEmail(request.email) != null) {
            return ResponseEntity.badRequest().body(Message("Email already used"))
        }
        val hashedPassword = passwordEncoder.encode(request.password)
        val user = User(
            username = request.username,
            email = request.email,
            password = hashedPassword,
            role = Role.USER,
            score = 0
        )
        userService.create(user)
        return ResponseEntity.ok(Message("Successfully register"))
    }

    @GetMapping("/me")
    fun validate(@CookieValue("jwt") token: String?): ResponseEntity<Any> {
        if (token == null) {
            return ResponseEntity.status(401).body(Message("Unauthenticated"))
        }
        return try {
            val id = jwtUtil.getUserIdFromToken(token)
            if (id == null) {
                return ResponseEntity.status(401).body(Message("Invalid or expired token"))
            }

            val user = this.userService.findById(id)
            if (user == null) {
                ResponseEntity.status(404).body(Message("User not found"))
            }                
            return ResponseEntity.ok(user)

        } catch (e: JwtException) {
            ResponseEntity.status(401).body(Message("Invalid or expired token"))
        }
    }

    @PostMapping ("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<Any> {
        val cookie = Cookie("jwt", "")
        cookie.maxAge = 0
        cookie.path = "/"
        cookie.isHttpOnly = true 
        response.addCookie(cookie)
        return ResponseEntity.ok(Message("success"))
    }
}