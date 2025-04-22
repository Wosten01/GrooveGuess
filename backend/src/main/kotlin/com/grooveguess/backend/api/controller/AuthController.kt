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
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.Jwts
import java.util.Date
import java.nio.charset.StandardCharsets
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${jwt.secret}") private val jwtSecret: String,
    @Value("\${jwt.expirationMs}") private val jwtExpirationMs: Long
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginDTO): ResponseEntity<Any> {
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

        val issuer = user.id.toString()

        val key = SecretKeySpec(jwtSecret.toByteArray(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.jcaName)
        val jwt = Jwts.builder()
            .setSubject(user.email)
            .setIssuer(issuer)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()

        return ResponseEntity.ok(AuthResponse(token = jwt, Message("Successfully login")))
    }

    @PostMapping("/register")
    fun register(request: RegisterDTO): ResponseEntity<Message> {
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
}