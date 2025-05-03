package com.grooveguess.backend.api.controller

import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import com.grooveguess.backend.domain.dto.RegisterDTO
import com.grooveguess.backend.domain.dto.LoginDTO
import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.repository.UserRepository
import com.grooveguess.backend.domain.enum.Role
import com.grooveguess.backend.service.UserService
import com.grooveguess.backend.domain.dto.Message
import com.grooveguess.backend.util.JwtUtil
import com.grooveguess.backend.util.CookieUtils
import io.jsonwebtoken.JwtException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val cookieUtils: CookieUtils,
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/login")
    fun login(@RequestBody request: LoginDTO, response: HttpServletResponse): ResponseEntity<Message> {
        logger.debug("Login attempt for email: {}", request.email)
        if (request.email.isBlank() || !request.email.contains("@")) {
            logger.warn("Login failed: Invalid email format for email: {}", request.email)
            return ResponseEntity.badRequest().body(Message("Invalid email format"))
        }
        if (request.password.isBlank()) {
            logger.warn("Login failed: Blank password for email: {}", request.email)
            return ResponseEntity.badRequest().body(Message("Password cannot be blank"))
        }
        val user = userService.findByEmail(request.email)
            ?: run {
                logger.warn("Login failed: User not found for email: {}", request.email)
                return ResponseEntity.status(401).body(Message("User not found"))
            }

        if (!userService.comparePassword(user, request.password)){
            logger.warn("Login failed: Invalid credentials for email: {}", request.email)
            return ResponseEntity.status(401).body(Message("Invalid credentials"))
        }

        val token = jwtUtil.generateToken(user)
        cookieUtils.addJwtCookie(response, token)

        logger.debug("User logged in successfully: {}", request.email)
        return ResponseEntity.ok(Message("Successfully login"))
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterDTO): ResponseEntity<Message> {
        logger.debug("Registration attempt for email: {}, username: {}", request.email, request.username)
        if (request.email.isBlank() || !request.email.contains("@")) {
            logger.warn("Registration failed: Invalid email format for email: {}", request.email)
            return ResponseEntity.badRequest().body(Message("Invalid email format"))
        }
        if (request.password.isBlank()) {
            logger.warn("Registration failed: Blank password for email: {}", request.email)
            return ResponseEntity.badRequest().body(Message("Password cannot be blank"))
        }
        if (request.username.isBlank()) {
            logger.warn("Registration failed: Blank username for email: {}", request.email)
            return ResponseEntity.badRequest().body(Message("Username cannot be blank"))
        }
        if (userService.findByEmail(request.email) != null) {
            logger.warn("Registration failed: Email already used: {}", request.email)
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
        logger.debug("User registered successfully: {}", request.email)
        return ResponseEntity.ok(Message("Successfully register"))
    }

    @GetMapping("/me")
    fun validate(@CookieValue("jwt") token: String?): ResponseEntity<Any> {
        logger.debug("Token validation attempt")
        if (token == null) {
            logger.warn("Token validation failed: No token provided")
            return ResponseEntity.status(401).body(Message("Unauthenticated"))
        }
        return try {
            val id = jwtUtil.getUserIdFromToken(token)
            if (id == null) {
                logger.warn("Token validation failed: Invalid or expired token")
                return ResponseEntity.status(401).body(Message("Invalid or expired token"))
            }

            val user = this.userService.findById(id)
            if (user == null) {
                logger.warn("Token validation failed: User not found for id: {}", id)
                return ResponseEntity.status(404).body(Message("User not found"))
            }
            logger.debug("Token validated successfully for user id: {}", id)
            ResponseEntity.ok(user)

        } catch (e: JwtException) {
            logger.warn("Token validation failed: Exception - {}", e.message)
            ResponseEntity.status(401).body(Message("Invalid or expired token"))
        }
    }

    @PostMapping("/logout")
    fun logout(response: HttpServletResponse): ResponseEntity<Any> {
        logger.debug("Logout attempt")
        cookieUtils.clearJwtCookie(response)
        logger.debug("Logout successful")
        return ResponseEntity.ok(Message("success"))
    }
}