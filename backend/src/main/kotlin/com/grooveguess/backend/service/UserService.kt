package com.grooveguess.backend.service

import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.repository.UserRepository
import com.grooveguess.backend.domain.enum.Role
import com.grooveguess.backend.domain.dto.RegisterDTO
import com.grooveguess.backend.domain.dto.LoginDTO
import org.springframework.stereotype.Service
import org.springframework.security.crypto.password.PasswordEncoder
import org.slf4j.LoggerFactory
import jakarta.transaction.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun create(user: User): User = userRepository.save(user)

    fun find(id: Long): User = userRepository.findById(id)
        .orElseThrow { RuntimeException("User not found") }

    fun findById(id: Long): User? = userRepository.findById(id).orElse(null)

    fun findByEmail(email: String): User? = userRepository.findByEmail(email)

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

    fun setScore(userId: Long, score: Int): User? {
        return userRepository.findById(userId).map {
            val updatedUser = it.copy(score = score)
            userRepository.save(updatedUser)
        }.orElse(null)
    }

    @Transactional
    fun addScore(userId: Long, addedScore: Int): User? {
        logger.debug("Adding $addedScore points to user $userId")
        try {
            val userOptional = userRepository.findById(userId)
            
            if (userOptional.isEmpty) {
                logger.warn("User not found with ID: $userId")
                return null
            }
            
            val user = userOptional.get()
            val newScore = user.score + addedScore
            logger.debug("Updating score from ${user.score} to $newScore")
            
            val updatedUser = user.copy(score = newScore)
            return userRepository.save(updatedUser)
        } catch (e: Exception) {
            logger.error("Error adding score for user $userId: ${e.message}", e)
            throw e
        }
    }

    fun delete(id: Long) {
        userRepository.deleteById(id)
    }

    fun isAdmin(userId: Long): Boolean {
        val user = find(userId)
        return user.role == Role.ADMIN
    }

    fun comparePassword(user: User, rawPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, user.password)
    }
}