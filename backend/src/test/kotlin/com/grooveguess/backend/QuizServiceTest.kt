package com.grooveguess.service

import com.grooveguess.domain.model.Quiz
import com.grooveguess.domain.model.Track
import com.grooveguess.domain.model.User
import com.grooveguess.domain.repository.QuizRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.argThat
import java.util.Optional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

@ExtendWith(MockitoExtension::class)
class QuizServiceTest {

    @Mock
    private lateinit var quizRepository: QuizRepository

    @Mock
    private lateinit var userService: UserService

    @InjectMocks
    private lateinit var quizService: QuizService

    @Test
    fun `create should save quiz when user is admin`() {
        val track = Track(id = 1, title = "Song", artist = "Artist", url = "http://example.com")
        val quiz = Quiz(id = 0, title = "Test Quiz", description = "Desc", roundCount = 1, tracks = listOf(track))
        val savedQuiz = quiz.copy(id = 1)
        whenever(userService.isAdmin(1)).thenReturn(true)
        whenever(quizRepository.save(quiz)).thenReturn(savedQuiz)

        val result = quizService.create(quiz, 1)

        assertEquals(savedQuiz, result)
        verify(quizRepository).save(quiz)
    }

    @Test
    fun `create should throw exception when user is not admin`() {
        val quiz = Quiz(id = 0, title = "Test Quiz", description = "Desc", roundCount = 1, tracks = listOf())
        whenever(userService.isAdmin(1)).thenReturn(false)

        val exception = assertThrows<IllegalAccessException> {
            quizService.create(quiz, 1)
        }

        assertEquals("Only admins can create quizzes", exception.message)
    }

    @Test
    fun `create should throw exception when tracks count is less than roundCount`() {
        val quiz = Quiz(id = 0, title = "Test Quiz", description = "Desc", roundCount = 2, tracks = listOf())
        whenever(userService.isAdmin(1)).thenReturn(true)

        val exception = assertThrows<IllegalArgumentException> {
            quizService.create(quiz, 1)
        }

        assertEquals("Quiz must have at least 2 tracks", exception.message)
    }

    @Test
    fun `submitAnswer should add 10 points for correct answer`() {
        val user = User(id = 1, username = "user", password = "pass", role = "USER", email = "user@example.com", score = 50)
        val track = Track(id = 1, title = "Song", artist = "Artist", url = "http://example.com")
        val quiz = Quiz(id = 1, title = "Test Quiz", description = "Desc", roundCount = 1, tracks = listOf(track))
        val updatedUser = user.copy(score = 60)
        whenever(quizRepository.findById(1L)).thenReturn(Optional.of(quiz))
        whenever(userService.find(1L)).thenReturn(user)
        whenever(userService.update(1L, updatedUser)).thenReturn(updatedUser)

        val result = quizService.submitAnswer(1L, 1L, 1L, true)

        assertEquals(60, result)
        verify(userService).update(1L, argThat { this.score == 60 })
    }
}
