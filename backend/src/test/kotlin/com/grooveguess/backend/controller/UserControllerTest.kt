package com.grooveguess.backend.controller

import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.enum.Role
import com.grooveguess.backend.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

class UserControllerTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userController: UserController

    private val user1 = User(id = 1, username = "alice", email = "alice@example.com", password="aboba1221", score = 100, role = Role.USER)
    private val user2 = User(id = 2, username = "bob", email = "bob@example.com", password="aboba1221", score = 200, role = Role.USER)
    private val user3 = User(id = 3, username = "admin", email = "admin@example.com", password="aboba1221", score = 999, role = Role.ADMIN)

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        userController = UserController(userRepository)
    }

    @Test
    fun `returns paginated users sorted by score desc without search`() {
        val pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "score"))
        whenever(userRepository.findByRole(Role.USER, pageable))
            .thenReturn(PageImpl(listOf(user2, user1), pageable, 2))

        val result = userController.getUsers(0, 2, null)
        assertEquals(2, result.content.size)
        assertEquals("bob", result.content[0].username)
        assertEquals("alice", result.content[1].username)
        verify(userRepository).findByRole(Role.USER, pageable)
    }

    @Test
    fun `returns paginated users with search by username`() {
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "score"))
        whenever(
            userRepository.findByRoleAndUsernameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                Role.USER, "ali", Role.USER, "ali", pageable
            )
        ).thenReturn(PageImpl(listOf(user1), pageable, 1))  
    
        val result = userController.getUsers(0, 20, "ali")
        assertEquals(1, result.content.size)
        assertEquals("alice", result.content[0].username)
        verify(userRepository).findByRoleAndUsernameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
            Role.USER, "ali", Role.USER, "ali", pageable
        )
    }
    
    @Test
    fun `returns paginated users with search by email`() {
        val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "score"))
        whenever(
            userRepository.findByRoleAndUsernameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
                Role.USER, "bob@", Role.USER, "bob@", pageable
            )
        ).thenReturn(PageImpl(listOf(user2), pageable, 1)) 
    
        val result = userController.getUsers(0, 20, "bob@")
        assertEquals(1, result.content.size)
        assertEquals("bob", result.content[0].username)
        verify(userRepository).findByRoleAndUsernameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
            Role.USER, "bob@", Role.USER, "bob@", pageable
        )
    }

    @Test
    fun `does not return users with role ADMIN`() {
        val pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "score"))
        whenever(userRepository.findByRole(Role.USER, pageable))
            .thenReturn(PageImpl(listOf(user1, user2), pageable, 2))

        val result = userController.getUsers(0, 10, "")
        assertTrue(result.content.none { it.role == Role.ADMIN })
        verify(userRepository).findByRole(Role.USER, pageable)
    }
}