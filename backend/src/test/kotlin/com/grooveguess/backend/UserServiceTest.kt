package com.grooveguess.backend.service

import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.repository.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var userService: UserService

    @Test
    fun `create should save and return user`() {
        val user = User(id = 0, username = "test", password = "pass", role = "USER", email = "test@example.com")
        val savedUser = user.copy(id = 1)
        whenever(userRepository.save(user)).thenReturn(savedUser)

        val result = userService.create(user)

        assertEquals(savedUser, result)
        verify(userRepository).save(user)
    }

    @Test
    fun `find should return user when found`() {
        val user = User(id = 1, username = "test", password = "pass", role = "USER", email = "test@example.com")
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
        val existingUser = User(id = 1, username = "old", password = "pass", role = "USER", email = "old@example.com")
        val updatedUser = User(id = 1, username = "new", password = "newpass", role = "ADMIN", email = "new@example.com")
        val savedUser = updatedUser.copy(id = 1)
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(existingUser))
        whenever(userRepository.save(savedUser)).thenReturn(savedUser)

        val result = userService.update(1L, updatedUser)

        assertEquals(savedUser, result)
        verify(userRepository).findById(1L)
        verify(userRepository).save(savedUser)
    }

    @Test
    fun `update should return null when user not found`() {
        whenever(userRepository.findById(1L)).thenReturn(Optional.empty())

        val result = userService.update(1L, User(username = "test", password = "pass", role = "USER", email = "test@example.com"))

        assertEquals(null, result)
        verify(userRepository).findById(1L)
    }

    @Test
    fun `delete should call repository deleteById`() {
        userService.delete(1L)

        verify(userRepository).deleteById(1L)
    }
}