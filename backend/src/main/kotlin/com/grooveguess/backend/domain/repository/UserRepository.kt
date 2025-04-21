package com.grooveguess.backend.domain.repository

import com.grooveguess.backend.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
}