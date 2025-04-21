
package com.grooveguess.backend.service

import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.enum.Role
import com.grooveguess.backend.domain.repository.UserRepository
import com.grooveguess.backend.domain.dto.RegisterRequest
import com.grooveguess.backend.domain.dto.LoginRequest
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
        val user = User(id = null, username = "test", password = "pass", role = Role.USER, email = "test@example.com", score = 0)
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

    @Test
    fun `login should return user when credentials are correct`() {
        val user = User(id = 1L, username = "test", password = "hashed", role = Role.USER, email = "test@example.com", score = 0)
        val request = LoginRequest(email = "test@example.com", password = "plain")
        whenever(userRepository.findByEmail("test@example.com")).thenReturn(user)
        whenever(passwordEncoder.matches("plain", "hashed")).thenReturn(true)

        val result = userService.login(request)

        assertEquals(user, result)
        verify(userRepository).findByEmail("test@example.com")
        verify(passwordEncoder).matches("plain", "hashed")
    }

    @Test
    fun `login should throw exception when user not found`() {
        val request = LoginRequest(email = "notfound@example.com", password = "plain")
        whenever(userRepository.findByEmail("notfound@example.com")).thenReturn(null)

        val exception = assertThrows<IllegalArgumentException> {
            userService.login(request)
        }

        assertEquals("Invalid credentials", exception.message)
        verify(userRepository).findByEmail("notfound@example.com")
    }

    @Test
    fun `login should throw exception when password does not match`() {
        val user = User(id = 1L, username = "test", password = "hashed", role = Role.USER, email = "test@example.com", score = 0)
        val request = LoginRequest(email = "test@example.com", password = "wrong")
        whenever(userRepository.findByEmail("test@example.com")).thenReturn(user)
        whenever(passwordEncoder.matches("wrong", "hashed")).thenReturn(false)

        val exception = assertThrows<IllegalArgumentException> {
            userService.login(request)
        }

        assertEquals("Invalid credentials", exception.message)
        verify(userRepository).findByEmail("test@example.com")
        verify(passwordEncoder).matches("wrong", "hashed")
    }

    @Test
    fun `register should save and return user when email is unique`() {
        val request = RegisterRequest(username = "test", email = "test@example.com", password = "plain")
        whenever(userRepository.findByEmail("test@example.com")).thenReturn(null)
        whenever(passwordEncoder.encode("plain")).thenReturn("hashed")
        val userToSave = User(username = "test", email = "test@example.com", password = "hashed", role = Role.USER, score = 0)
        val savedUser = userToSave.copy(id = 1L)
        whenever(userRepository.save(userToSave)).thenReturn(savedUser)

        val result = userService.register(request)

        assertEquals(savedUser, result)
        verify(userRepository).findByEmail("test@example.com")
        verify(passwordEncoder).encode("plain")
        verify(userRepository).save(userToSave)
    }

    @Test
    fun `register should throw exception when email already exists`() {
        val request = RegisterRequest(username = "test", email = "test@example.com", password = "plain")
        val existingUser = User(id = 1L, username = "test", password = "hashed", role = Role.USER, email = "test@example.com", score = 0)
        whenever(userRepository.findByEmail("test@example.com")).thenReturn(existingUser)

        val exception = assertThrows<IllegalArgumentException> {
            userService.register(request)
        }

        assertEquals("Email already exists", exception.message)
        verify(userRepository).findByEmail("test@example.com")
        verify(passwordEncoder, never()).encode(any())
        verify(userRepository, never()).save(any())
    }
}
