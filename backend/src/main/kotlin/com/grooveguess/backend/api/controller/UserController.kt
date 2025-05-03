package com.grooveguess.backend.controller

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.enum.Role
import com.grooveguess.backend.domain.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(UserController::class.java)
    
    @GetMapping
    fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false, defaultValue = "") search: String?
    ): Page<User> {
        logger.debug("GET /api/users - Fetching users with page=$page, size=$size, search=$search")
        
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "score"))
        val role = Role.USER
        
        val result = if (!search.isNullOrBlank()) {
            logger.debug("Searching for users with role=$role and search term='$search'")
            userRepository.findByRoleAndUsernameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                role, search, role, search, pageable
            )
        } else {
            logger.debug("Fetching all users with role=$role")
            userRepository.findByRole(role, pageable)
        }
        
        logger.debug("Found ${result.totalElements} users, page ${result.number + 1} of ${result.totalPages}")
        return result
    }
}