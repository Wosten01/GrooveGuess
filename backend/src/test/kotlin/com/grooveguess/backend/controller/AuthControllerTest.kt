package com.grooveguess.backend.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.grooveguess.backend.domain.dto.LoginDTO
import com.grooveguess.backend.domain.dto.RegisterDTO
import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.enum.Role
import com.grooveguess.backend.domain.repository.UserRepository
import com.grooveguess.backend.service.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var userService: UserService

    @MockitoBean
    private lateinit var passwordEncoder: PasswordEncoder

    @MockitoBean
    private lateinit var userRepository: UserRepository

    private val testUser = User(
        id = 1,
        username = "testuser",
        email = "test@example.com",
        password = "encodedPassword",
        role = Role.USER,
        score = 0
    )

    @BeforeEach
    fun setup() {
        Mockito.reset(userService, passwordEncoder, userRepository)
    }

    @Test
    fun `login should return 200 for valid credentials`() {
        val loginDTO = LoginDTO(email = "test@example.com", password = "password")
        Mockito.`when`(userService.findByEmail("test@example.com")).thenReturn(testUser)
        Mockito.`when`(userService.comparePassword(testUser, "password")).thenReturn(true)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Successfully login"))
    }

    @Test
    fun `login should return 401 for invalid credentials`() {
        val loginDTO = LoginDTO(email = "test@example.com", password = "wrongpassword")
        Mockito.`when`(userService.findByEmail("test@example.com")).thenReturn(testUser)
        Mockito.`when`(userService.comparePassword(testUser, "wrongpassword")).thenReturn(false)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("Invalid credentials"))
    }

    @Test
    fun `login should return 401 if user not found`() {
        val loginDTO = LoginDTO(email = "notfound@example.com", password = "password")
        Mockito.`when`(userService.findByEmail("notfound@example.com")).thenReturn(null)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("User not found"))
    }

    @Test
    fun `register should return 200 for valid data`() {
        val registerDTO = RegisterDTO(email = "new@example.com", password = "password", username = "newuser")
        Mockito.`when`(userService.findByEmail("new@example.com")).thenReturn(null)
        Mockito.`when`(passwordEncoder.encode("password")).thenReturn("encodedPassword")

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Successfully register"))
    }

    @Test
    fun `register should return 400 if email already used`() {
        val registerDTO = RegisterDTO(email = "test@example.com", password = "password", username = "testuser")
        Mockito.`when`(userService.findByEmail("test@example.com")).thenReturn(testUser)

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Email already used"))
    }

    @Test
    fun `register should return 400 for blank username`() {
        val registerDTO = RegisterDTO(email = "test@example.com", password = "password", username = "")
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Username cannot be blank"))
    }

    @Test
    fun `logout should return 200`() {
        mockMvc.perform(
            post("/api/auth/logout")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("success"))
    }

    @Test
    fun `validate should return 401 if no token`() {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.message").value("Unauthenticated"))
    }
}