package com.grooveguess.backend.service

import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.repository.UserRepository
import com.grooveguess.backend.domain.enum.Role
import com.grooveguess.backend.domain.dto.RegisterRequest
import com.grooveguess.backend.domain.dto.LoginRequest
import org.springframework.stereotype.Service
import org.springframework.security.crypto.password.PasswordEncoder

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun create(user: User): User = userRepository.save(user)

    fun find(id: Long): User = userRepository.findById(id)
        .orElseThrow { RuntimeException("User not found") }

    fun update(id: Long, updatedUser: User): User? {
        return userRepository.findById(id).map {
            val newUser = it.copy(
                username = updatedUser.username,
                password = updatedUser.password,
                role = updatedUser.role,
                score = updatedUser.score,
                email = updatedUser.email
            )
            userRepository.save(newUser)
        }.orElse(null)
    }

    fun delete(id: Long) {
        userRepository.deleteById(id)
    }

    fun isAdmin(userId: Long): Boolean {
        val user = find(userId)
        return user.role == Role.ADMIN
    }

    fun login(request: LoginRequest): User {
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("Invalid credentials")
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("Invalid credentials")
        }
        return user
    }

    fun register(request: RegisterRequest): User {
        if (userRepository.findByEmail(request.email) != null) {
            throw IllegalArgumentException("Email already exists")
        }
        val user = User(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            role = Role.USER,
            score = 0
        )
        return create(user)
    }
}