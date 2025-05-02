package com.grooveguess.backend.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.grooveguess.backend.domain.dto.*
import com.grooveguess.backend.service.GameService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.*
import com.grooveguess.backend.exception.AccessDeniedException

@WebMvcTest(GameController::class)
@AutoConfigureMockMvc(addFilters = false)
class GameControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var gameService: GameService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `startGame should return game session when successful`() {
        val quizId = 1L
        val userId = 1L
        val sessionDto = GameSessionDto(
            sessionId = "test-session-id",
            totalRounds = 3,
            currentRoundNumber = 0,
            score = 0,
            completed = false,
            currentRound = RoundDto(
                currentRound = 0,
                url = "http://example.com/track1.mp3",
                options = listOf(
                    TrackOptionDto(id = 1L, title = "Track 1", artist = "Artist 1"),
                    TrackOptionDto(id = 2L, title = "Track 2", artist = "Artist 2")
                )
            )
        )

        `when`(gameService.startGame(quizId, userId)).thenReturn(sessionDto)

        mockMvc.perform(post("/api/quiz-game/$quizId/start")
                .with(csrf())
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.sessionId").value("test-session-id"))
            .andExpect(jsonPath("$.totalRounds").value(3))
            .andExpect(jsonPath("$.currentRoundNumber").value(0))
            .andExpect(jsonPath("$.score").value(0))
            .andExpect(jsonPath("$.completed").value(false))
            .andExpect(jsonPath("$.currentRound.currentRound").value(0))
            .andExpect(jsonPath("$.currentRound.url").value("http://example.com/track1.mp3"))
            .andExpect(jsonPath("$.currentRound.options.length()").value(2))
    }

    @Test
    fun `startGame should return bad request when quiz not found`() {
        val quizId = 999L
        val userId = 1L

        `when`(gameService.startGame(quizId, userId)).thenThrow(IllegalArgumentException("Quiz not found with id: $quizId"))

        mockMvc.perform(post("/api/quiz-game/$quizId/start")
                .with(csrf())
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Quiz not found with id: $quizId"))
    }

    @Test
    fun `getNextRound should return next round when successful`() {
        val sessionId = "test-session-id"
        val userId = 1L
        val roundDto = RoundDto(
            currentRound = 1,
            url = "http://example.com/track2.mp3",
            options = listOf(
                TrackOptionDto(id = 3L, title = "Track 3", artist = "Artist 3"),
                TrackOptionDto(id = 4L, title = "Track 4", artist = "Artist 4")
            )
        )

        `when`(gameService.getNextRound(sessionId, userId)).thenReturn(roundDto)

        mockMvc.perform(get("/api/quiz-game/player/$userId/session/$sessionId/next-round")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.currentRound").value(1))
            .andExpect(jsonPath("$.url").value("http://example.com/track2.mp3"))
            .andExpect(jsonPath("$.options.length()").value(2))
    }

    @Test
    fun `getNextRound should return not found when session not found`() {
        val sessionId = "non-existent-session"
        val userId = 1L

        `when`(gameService.getNextRound(sessionId, userId)).thenThrow(IllegalStateException("Session not found"))

        mockMvc.perform(get("/api/quiz-game/player/$userId/session/$sessionId/next-round")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Session not found"))
    }

    @Test
    fun `getNextRound should return no content when no more rounds available`() {
        val sessionId = "test-session-id"
        val userId = 1L

        `when`(gameService.getNextRound(sessionId, userId)).thenThrow(IllegalStateException("No more rounds available"))

        mockMvc.perform(get("/api/quiz-game/player/$userId/session/$sessionId/next-round")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent)
            .andExpect(jsonPath("$.message").value("No more rounds available"))
    }

    @Test
    fun `getNextRound should return conflict when game is already completed`() {
        val sessionId = "test-session-id"
        val userId = 1L

        `when`(gameService.getNextRound(sessionId, userId)).thenThrow(IllegalStateException("Game is already completed"))

        mockMvc.perform(get("/api/quiz-game/player/$userId/session/$sessionId/next-round")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("Game is already completed"))
    }

    @Test
    fun `getNextRound should return forbidden when user has no access`() {
        val sessionId = "test-session-id"
        val userId = 999L

        `when`(gameService.getNextRound(sessionId, userId))
            .thenThrow(AccessDeniedException("You don't have access to this session"))

        mockMvc.perform(get("/api/quiz-game/player/$userId/session/$sessionId/next-round")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.message").value("You don't have access to this session"))
    }

    @Test
    fun `submitAnswer should return result when successful`() {
        val sessionId = "test-session-id"
        val userId = 1L
        val answerDto = AnswerDto(roundNumber = 1, optionId = 3L)
        val resultDto = AnswerResultDto(
            correct = true,
            points = 10,
            isLastRound = false
        )

        `when`(gameService.submitAnswer(sessionId, answerDto, userId)).thenReturn(resultDto)

        mockMvc.perform(post("/api/quiz-game/player/$userId/session/$sessionId/answer")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerDto)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.correct").value(true))
            .andExpect(jsonPath("$.points").value(10))
            .andExpect(jsonPath("$.isLastRound").value(false))
    }

    @Test
    fun `submitAnswer should return result with finalScore when last round`() {
        val sessionId = "test-session-id"
        val userId = 1L
        val answerDto = AnswerDto(roundNumber = 2, optionId = 5L)
        val resultDto = AnswerResultDto(
            correct = true,
            points = 10,
            isLastRound = true,
            finalScore = 30
        )

        `when`(gameService.submitAnswer(sessionId, answerDto, userId)).thenReturn(resultDto)

        mockMvc.perform(post("/api/quiz-game/player/$userId/session/$sessionId/answer")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerDto)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.correct").value(true))
            .andExpect(jsonPath("$.points").value(10))
            .andExpect(jsonPath("$.isLastRound").value(true))
            .andExpect(jsonPath("$.finalScore").value(30))
    }

    @Test
    fun `submitAnswer should return bad request when invalid round number`() {
        val sessionId = "test-session-id"
        val userId = 1L
        val answerDto = AnswerDto(roundNumber = 999, optionId = 3L)

        `when`(gameService.submitAnswer(sessionId, answerDto, userId)).thenThrow(IllegalArgumentException("Invalid round number"))

        mockMvc.perform(post("/api/quiz-game/player/$userId/session/$sessionId/answer")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerDto)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Invalid round number"))
    }

    @Test
    fun `submitAnswer should return conflict when game is already completed`() {
        val sessionId = "test-session-id"
        val userId = 1L
        val answerDto = AnswerDto(roundNumber = 1, optionId = 3L)

        `when`(gameService.submitAnswer(sessionId, answerDto, userId)).thenThrow(IllegalStateException("Game is already completed"))

        mockMvc.perform(post("/api/quiz-game/player/$userId/session/$sessionId/answer")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerDto)))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("Game is already completed"))
    }

    @Test
    fun `submitAnswer should return forbidden when user has no access`() {
        val sessionId = "test-session-id"
        val userId = 999L
        val answerDto = AnswerDto(roundNumber = 1, optionId = 3L)

        `when`(gameService.submitAnswer(sessionId, answerDto, userId))
            .thenThrow(AccessDeniedException("You don't have access to this session"))

        mockMvc.perform(post("/api/quiz-game/player/$userId/session/$sessionId/answer")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerDto)))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.message").value("You don't have access to this session"))
    }

    @Test
    fun `getGameResults should return results when successful`() {
        val sessionId = "test-session-id"
        val userId = 1L
        val resultsDto = GameResultsDto(
            quizId = 1L,
            totalRounds = 3,
            score = 20,
            tracks = listOf(
                TrackResultDto(
                    roundNumber = 0,
                    trackId = 1L,
                    title = "Track 1",
                    artist = "Artist 1",
                    url = "http://example.com/track1.mp3",
                    wasGuessed = true,
                    options = listOf(
                        TrackOptionDto(id = 1L, title = "Track 1", artist = "Artist 1"),
                        TrackOptionDto(id = 2L, title = "Track 2", artist = "Artist 2")
                    )
                ),
                TrackResultDto(
                    roundNumber = 1,
                    trackId = 3L,
                    title = "Track 3",
                    artist = "Artist 3",
                    url = "http://example.com/track3.mp3",
                    wasGuessed = true,
                    options = listOf(
                        TrackOptionDto(id = 3L, title = "Track 3", artist = "Artist 3"),
                        TrackOptionDto(id = 4L, title = "Track 4", artist = "Artist 4")
                    )
                ),
                TrackResultDto(
                    roundNumber = 2,
                    trackId = 5L,
                    title = "Track 5",
                    artist = "Artist 5",
                    url = "http://example.com/track5.mp3",
                    wasGuessed = false,
                    options = listOf(
                        TrackOptionDto(id = 5L, title = "Track 5", artist = "Artist 5"),
                        TrackOptionDto(id = 6L, title = "Track 6", artist = "Artist 6")
                    )
                )
            )
        )

        `when`(gameService.getGameResults(sessionId, userId)).thenReturn(resultsDto)

        mockMvc.perform(get("/api/quiz-game/player/$userId/session/$sessionId/results")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.quizId").value(1))
            .andExpect(jsonPath("$.totalRounds").value(3))
            .andExpect(jsonPath("$.score").value(20))
            .andExpect(jsonPath("$.tracks.length()").value(3))
            .andExpect(jsonPath("$.tracks[0].roundNumber").value(0))
            .andExpect(jsonPath("$.tracks[0].wasGuessed").value(true))
            .andExpect(jsonPath("$.tracks[2].wasGuessed").value(false))
    }

    @Test
    fun `getGameResults should return not found when session not found`() {
        val sessionId = "non-existent-session"
        val userId = 1L

        `when`(gameService.getGameResults(sessionId, userId)).thenThrow(IllegalStateException("Session not found"))

        mockMvc.perform(get("/api/quiz-game/player/$userId/session/$sessionId/results")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Session not found"))
    }

    @Test
    fun `getGameResults should return forbidden when user has no access`() {
        val sessionId = "test-session-id"
        val userId = 999L

        `when`(gameService.getGameResults(sessionId, userId))
            .thenThrow(AccessDeniedException("You don't have access to this session"))

        mockMvc.perform(get("/api/quiz-game/player/$userId/session/$sessionId/results")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.message").value("You don't have access to this session"))
    }
}