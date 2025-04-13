package com.grooveguess.service

import com.grooveguess.domain.model.User
import com.grooveguess.domain.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

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
}