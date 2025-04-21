package com.grooveguess.backend.service

import com.grooveguess.backend.domain.model.Quiz
import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.repository.QuizRepository
import com.grooveguess.backend.domain.repository.TrackRepository
import com.grooveguess.backend.domain.enum.Role
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import java.util.Optional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.stubbing.Answer
import com.grooveguess.backend.domain.enum.AnswerStatus

@ExtendWith(MockitoExtension::class)
class QuizServiceTest {

    @Mock
    private lateinit var quizRepository: QuizRepository

    @Mock
    private lateinit var userService: UserService

    @Mock
    private lateinit var trackRepository: TrackRepository

    @InjectMocks
    private lateinit var quizService: QuizService

    @Test
    fun `create should save quiz when user is admin`() {
        val track = Track(id = 1L, title = "Song", artist = "Artist", url = "http://example.com")
        val quiz = Quiz(id = 0L, title = "Test Quiz", description = "Desc", roundCount = 1, tracks = listOf(track))
        val savedQuiz = quiz.copy(id = 1L)
        whenever(userService.isAdmin(1L)).thenReturn(true)
        whenever(quizRepository.save(quiz)).thenReturn(savedQuiz)

        val result = quizService.create(quiz, 1L)

        assertEquals(savedQuiz, result)
        verify(quizRepository).save(quiz)
    }

    @Test
    fun `create should throw exception when user is not admin`() {
        val quiz = Quiz(id = 0L, title = "Test Quiz", description = "Desc", roundCount = 1, tracks = listOf())
        whenever(userService.isAdmin(1L)).thenReturn(false)

        val exception = assertThrows<IllegalAccessException> {
            quizService.create(quiz, 1L)
        }

        assertEquals("Only admins can create quizzes", exception.message)
    }

    @Test
    fun `create should throw exception when tracks count is less than roundCount`() {
        val quiz = Quiz(id = 0L, title = "Test Quiz", description = "Desc", roundCount = 2, tracks = listOf())
        whenever(userService.isAdmin(1)).thenReturn(true)

        val exception = assertThrows<IllegalArgumentException> {
            quizService.create(quiz, 1L)
        }

        assertEquals("Quiz must have at least 2 tracks", exception.message)
    }

    @Test
    fun `submitAnswer returns CORRECT when selected track is correct`() {
        val user = User(id = 1L, username = "user", password = "pass", role = Role.USER, email = "user@example.com", score = 50)
        val track = Track(id = 1L, title = "Song", artist = "Artist", url = "http://example.com")
        val quiz = Quiz(id = 1L, title = "Test Quiz", description = "Desc", roundCount = 1, tracks = listOf(track))

        val quizId = quiz.id
        val userId = user.id!!
        val selectedTrackId = track.id
        val correctTrackId = track.id

        whenever(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz))
        whenever(userService.find(userId)).thenReturn(user)
        whenever(userService.update(eq(userId), any())).thenAnswer { invocation ->
            val updatedUser = invocation.getArgument(1) as User
            whenever(userService.find(userId)).thenReturn(updatedUser)
            updatedUser
        }

        val result = quizService.submitAnswer(quizId, userId, selectedTrackId, correctTrackId)

        assertEquals(AnswerStatus.CORRECT, result.status)
        assertEquals(60, userService.find(userId).score)
    }

    @Test
    fun `submitAnswer returns INCORRECT when selected track is incorrect`() {
        val user = User(id = 1L, username = "user", password = "pass", role = Role.USER, email = "user@example.com", score = 50)
        val track = Track(id = 1L, title = "Song", artist = "Artist", url = "http://example.com")
        val track2 = Track(id = 2L, title = "Song2", artist = "Artist2", url = "http://example2.com")
        val quiz = Quiz(id = 1L, title = "Test Quiz", description = "Desc", roundCount = 1, tracks = listOf(track, track2))

        val quizId = quiz.id
        val userId = user.id!!
        val selectedTrackId = track.id
        val correctTrackId = track2.id

        whenever(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz))
        whenever(userService.find(userId)).thenReturn(user)

        val result = quizService.submitAnswer(quizId, userId, selectedTrackId, correctTrackId)

        assertEquals(AnswerStatus.INCORRECT, result.status)
        assertEquals(50, userService.find(userId).score)
    }
}
