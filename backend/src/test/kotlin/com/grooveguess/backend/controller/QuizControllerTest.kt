
package com.grooveguess.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.grooveguess.backend.domain.dto.QuizRequest
import com.grooveguess.backend.domain.dto.QuizResponse
import com.grooveguess.backend.domain.model.Quiz
import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.service.QuizService
import com.grooveguess.backend.service.UserService
import com.grooveguess.backend.api.controller.QuizController
import com.grooveguess.backend.domain.repository.UserRepository
import com.grooveguess.backend.domain.repository.QuizRepository
import com.grooveguess.backend.domain.repository.TrackRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.*
import com.grooveguess.backend.domain.enum.Role
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.time.LocalDateTime
import java.util.*

class QuizServiceTest {

    private lateinit var quizService: QuizService
    private lateinit var quizRepository: QuizRepository
    private lateinit var trackRepository: TrackRepository
    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService
    private lateinit var user: User
    private lateinit var adminUser: User
    private lateinit var track: Track
    private lateinit var quiz: Quiz
    private val track1 = Track(id = 1, title = "Track1", artist = "Artist1", url="http://example.mp3")
    private val track2 = Track(id = 2, title = "Track2", artist = "Artist2", url="http://example2.mp3")
    private val quizRequest = QuizRequest(
        title = "Quiz1",
        description = "desc",
        roundCount = 2,
        trackIds = listOf(track1.id, track2.id)
    )

    @BeforeEach
    fun setUp() {
        quizRepository = mock()
        trackRepository = mock()
        userRepository = mock()
        userService = mock()
        quizService = QuizService(quizRepository, trackRepository, userService, userRepository)
            
        adminUser = User(id = 1, username = "admin", password = "pass", role = com.grooveguess.backend.domain.enum.Role.ADMIN, score = 0, email = "admin@test.com")

        user = User(
            id = 1L,
            username = "admin",
            password = "pass",
            role = Role.ADMIN,
            score = 0,
            email = "admin@test.com"
        )
        track = Track(id = 1L, title = "Track1", artist = "Artist1", url = "http://example.mp3")
        quiz = Quiz(
            id = 1L,
            title = "Quiz1",
            description = "desc",
            roundCount = 2, 
            tracks = listOf(track, track),
            creator = user,
            createdAt = LocalDateTime.now()
        )
    }

    @Test
    fun `update quiz as admin updates fields but not creator or createdAt`() {
        val now = LocalDateTime.now()
        val existingQuiz = Quiz(
            id = 1L,
            title = "Old Quiz",
            description = "old desc",
            roundCount = 2, 
            tracks = listOf(track, track),
            creator = user,
            createdAt = now
        )
        val updatedQuiz = Quiz(
            id = 1L,
            title = "New Quiz",
            description = "new desc",
            roundCount = 2, 
            tracks = listOf(track, track),
            creator = user,
            createdAt = now
        )
        given(userService.isAdmin(1L)).willReturn(true)
        given(quizRepository.findById(1L)).willReturn(Optional.of(existingQuiz))
        given(quizRepository.save(any(Quiz::class.java))).willReturn(updatedQuiz)

        val result = quizService.update(1L, updatedQuiz, 1L)

        assertEquals(updatedQuiz.title, result?.title)
        assertEquals(existingQuiz.creator, result?.creator)
        assertEquals(existingQuiz.createdAt, result?.createdAt)
    }
}