
package com.grooveguess.backend.service

import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.enum.Role
import com.grooveguess.backend.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var userService: UserService

    private val user = User(
        id = 1L,
        username = "testuser",
        password = "hashedPassword",
        role = Role.USER,
        email = "test@example.com",
        score = 100
    )

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        passwordEncoder = mock()
        userService = UserService(userRepository, passwordEncoder)
    }

    @Test
    fun `create should save and return user`() {
        whenever(userRepository.save(user)).thenReturn(user)
        val result = userService.create(user)
        assertEquals(user, result)
        verify(userRepository).save(user)
    }

    @Test
    fun `find should return user when found`() {
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
        val result = userService.find(1L)
        assertEquals(user, result)
        verify(userRepository).findById(1L)
    }

    @Test
    fun `find should throw when not found`() {
        whenever(userRepository.findById(2L)).thenReturn(Optional.empty())
        assertThrows(RuntimeException::class.java) {
            userService.find(2L)
        }
        verify(userRepository).findById(2L)
    }

    @Test
    fun `findById should return user when found`() {
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
        val result = userService.findById(1L)
        assertEquals(user, result)
    }

    @Test
    fun `findById should return null when not found`() {
        whenever(userRepository.findById(2L)).thenReturn(Optional.empty())
        val result = userService.findById(2L)
        assertNull(result)
    }

    @Test
    fun `findByEmail should return user when found`() {
        whenever(userRepository.findByEmail("test@example.com")).thenReturn(user)
        val result = userService.findByEmail("test@example.com")
        assertEquals(user, result)
    }

    @Test
    fun `findByEmail should return null when not found`() {
        whenever(userRepository.findByEmail("notfound@example.com")).thenReturn(null)
        val result = userService.findByEmail("notfound@example.com")
        assertNull(result)
    }

    @Test
    fun `update should update and return user when found`() {
        val updatedUser = user.copy(username = "newuser", password = "newpass", role = Role.ADMIN, score = 200, email = "new@example.com")
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
        whenever(userRepository.save(any<User>())).thenReturn(updatedUser)

        val result = userService.update(1L, updatedUser)
        assertEquals(updatedUser, result)
        verify(userRepository).findById(1L)
        verify(userRepository).save(any())
    }

    @Test
    fun `update should return null when user not found`() {
        val updatedUser = user.copy(username = "newuser")
        whenever(userRepository.findById(2L)).thenReturn(Optional.empty())

        val result = userService.update(2L, updatedUser)
        assertNull(result)
        verify(userRepository).findById(2L)
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `delete should call repository deleteById`() {
        userService.delete(1L)
        verify(userRepository).deleteById(1L)
    }

    @Test
    fun `isAdmin should return true if user is admin`() {
        val adminUser = user.copy(role = Role.ADMIN)
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(adminUser))
        val result = userService.isAdmin(1L)
        assertTrue(result)
    }

    @Test
    fun `isAdmin should return false if user is not admin`() {
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
        val result = userService.isAdmin(1L)
        assertFalse(result)
    }

    @Test
    fun `comparePassword should delegate to passwordEncoder`() {
        whenever(passwordEncoder.matches("raw", "hashedPassword")).thenReturn(true)
        val result = userService.comparePassword(user, "raw")
        assertTrue(result)
        verify(passwordEncoder).matches("raw", "hashedPassword")
    }
}
