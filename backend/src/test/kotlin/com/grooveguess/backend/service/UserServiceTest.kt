
package com.grooveguess.backend.service

import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.enum.Role
import com.grooveguess.backend.domain.repository.UserRepository
import com.grooveguess.backend.domain.dto.LoginDTO
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: BCryptPasswordEncoder
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        passwordEncoder = mock()
        userService = UserService(userRepository, passwordEncoder)
    }

    @Test
    fun `create should save and return user`() {
        val user = User(username = "test", password = "pass", role = Role.USER, email = "test@example.com", score = 0)
        val savedUser = user.copy(id = 1L)
        whenever(userRepository.save(user)).thenReturn(savedUser)

        val result = userService.create(user)

        assertEquals(savedUser, result)
        verify(userRepository).save(user)
    }

    @Test
    fun `find should return user when found`() {
        val user = User(id = 1L, username = "test", password = "pass", role = Role.USER, email = "test@example.com", score = 0)
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

        val result = userService.find(1L)

        assertEquals(user, result)
        verify(userRepository).findById(1L)
    }

    @Test
    fun `find should throw exception when user not found`() {
        whenever(userRepository.findById(1L)).thenReturn(Optional.empty())

        val exception = assertThrows<RuntimeException> {
            userService.find(1L)
        }

        assertEquals("User not found", exception.message)
        verify(userRepository).findById(1L)
    }

    @Test
    fun `update should update and return user when found`() {
        val oldUser = User(id = 1L, username = "old", password = "oldpass", role = Role.USER, email = "old@example.com", score = 0)
        val updatedUser = User(id = 1L, username = "new", password = "newpass", role = Role.ADMIN, email = "new@example.com", score = 10)
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(oldUser))
        whenever(userRepository.save(any<User>())).thenReturn(updatedUser)

        val result = userService.update(1L, updatedUser)

        assertEquals(updatedUser, result)
        verify(userRepository).findById(1L)
        verify(userRepository).save(any())
    }

    @Test
    fun `update should return null when user not found`() {
        val updatedUser = User(id = 1L, username = "new", password = "newpass", role = Role.ADMIN, email = "new@example.com", score = 10)
        whenever(userRepository.findById(1L)).thenReturn(Optional.empty())

        val result = userService.update(1L, updatedUser)

        assertNull(result)
        verify(userRepository).findById(1L)
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `delete should call repository deleteById`() {
        userService.delete(1L)
        verify(userRepository).deleteById(1L)
    }

    @Test
    fun `isAdmin should return true if user is admin`() {
        val user = User(id = 1L, username = "admin", password = "pass", role = Role.ADMIN, email = "admin@example.com", score = 0)
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

        val result = userService.isAdmin(1L)

        assertTrue(result)
        verify(userRepository).findById(1L)
    }

    @Test
    fun `isAdmin should return false if user is not admin`() {
        val user = User(id = 1L, username = "user", password = "pass", role = Role.USER, email = "user@example.com", score = 0)
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

        val result = userService.isAdmin(1L)

        assertFalse(result)
        verify(userRepository).findById(1L)
    }
}
