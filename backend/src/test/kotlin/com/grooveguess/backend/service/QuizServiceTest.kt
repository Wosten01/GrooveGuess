package com.grooveguess.backend.service

import com.grooveguess.backend.domain.model.Quiz
import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.repository.QuizRepository
import com.grooveguess.backend.domain.repository.TrackRepository
import com.grooveguess.backend.domain.repository.UserRepository
import com.grooveguess.backend.domain.dto.QuizRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.util.*

class QuizServiceTest {

    private lateinit var quizRepository: QuizRepository
    private lateinit var trackRepository: TrackRepository
    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService
    private lateinit var quizService: QuizService

    private val adminUser = User(id = 1, username = "admin", password = "pass", role = com.grooveguess.backend.domain.enum.Role.ADMIN, score = 0, email = "admin@test.com")
    private val track1 = Track(id = 1, title = "Track1", artist = "Artist1", url="http://example.mp3")
    private val track2 = Track(id = 2, title = "Track2", artist = "Artist2", url="http://example2.mp3")
    private val quizRequest = QuizRequest(
        title = "Quiz1",
        description = "desc",
        roundCount = 2,
        trackIds = listOf(track1.id, track2.id)
    )
    private val quiz = Quiz(
        id = 1,
        title = "Quiz1",
        description = "desc",
        roundCount = 1,
        tracks = listOf(track1),
        creator = adminUser,
        createdAt = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        quizRepository = mock(QuizRepository::class.java)
        trackRepository = mock(TrackRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        userService = mock(UserService::class.java)
        quizService = QuizService(quizRepository, trackRepository, userService, userRepository)
        // `when`(trackRepository.findAllById(listOf(track1.id, track2.id))).thenReturn(listOf(track1, track2))
    }

    @Test
    fun `create quiz as admin sets creator and createdAt`() {
        `when`(userService.isAdmin(adminUser.id)).thenReturn(true)
        `when`(userRepository.findById(adminUser.id)).thenReturn(Optional.of(adminUser))
        `when`(trackRepository.findAllById(listOf(track1.id, track2.id))).thenReturn(listOf(track1, track2))
        `when`(quizRepository.save(any(Quiz::class.java))).thenAnswer { it.arguments[0] }
        `when`(userRepository.findById(adminUser.id)).thenReturn(Optional.of(adminUser))

        val createdQuiz = quizService.create(quizRequest, adminUser.id)

        assertEquals(adminUser, createdQuiz.creator)
        assertNotNull(createdQuiz.createdAt)
        assertEquals("Quiz1", createdQuiz.title)
        assertEquals(listOf(track1, track2), createdQuiz.tracks)
    }

    @Test
    fun `create quiz as non-admin throws exception`() {
        `when`(userService.isAdmin(2L)).thenReturn(false)
        assertThrows(IllegalAccessException::class.java) {
            quizService.create(quizRequest, 2L)
        }
    }

    @Test
    fun `delete quiz as admin calls repository`() {
        `when`(userService.isAdmin(adminUser.id)).thenReturn(true)
        doNothing().`when`(quizRepository).deleteById(quiz.id)
        quizService.delete(quiz.id, adminUser.id)
        verify(quizRepository, times(1)).deleteById(quiz.id)
    }
}
