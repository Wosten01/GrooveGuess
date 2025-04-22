package com.grooveguess.backend.api.controller

import com.fasterxml.jackson.databind.ObjectMapper 
import com.grooveguess.backend.domain.dto.LoginDTO 
import com.grooveguess.backend.domain.dto.Message 
import com.grooveguess.backend.domain.dto.RegisterDTO 
import com.grooveguess.backend.domain.enum.Role 
import com.grooveguess.backend.domain.model.User 
import com.grooveguess.backend.domain.repository.UserRepository 
import com.grooveguess.backend.service.UserService 
import com.grooveguess.backend.util.CookieUtils 
import com.grooveguess.backend.util.JwtUtil 
import io.jsonwebtoken.JwtException 
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse 
import org.junit.jupiter.api.Test 
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.kotlin.eq
import org.mockito.kotlin.doNothing
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired 
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType 
import org.springframework.security.crypto.password.PasswordEncoder 
import org.springframework.test.web.servlet.MockMvc 
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.* 
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(AuthController::class) class AuthControllerTests {
}