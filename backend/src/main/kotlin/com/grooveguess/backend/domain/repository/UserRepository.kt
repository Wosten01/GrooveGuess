
package com.grooveguess.backend.domain.repository

import com.grooveguess.backend.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import com.grooveguess.backend.domain.enum.Role


interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?

    fun findUserById(id: Long): User? = findById(id).orElse(null)

    fun findByRole(role: Role, pageable: Pageable): Page<User>

    fun findByRoleAndUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        role: Role,
        username: String,
        email: String,
        pageable: Pageable
    ): Page<User>

    fun findByRoleAndUsernameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
        role1: Role, 
        username: String, 
        role2: Role, 
        email: String, 
        pageable: Pageable
    ): Page<User>
}
