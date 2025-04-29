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

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userRepository: UserRepository
) {
    @GetMapping
    fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false, defaultValue = "") search: String?
    ): Page<User> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "score"))
        val role = Role.USER
        return if (!search.isNullOrBlank()) {
            userRepository.findByRoleAndUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                role, search, search, pageable
            )
        } else {
            userRepository.findByRole(role, pageable)
        }
    }
}