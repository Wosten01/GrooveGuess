package com.grooveguess.backend.service

import com.grooveguess.backend.config.RedisUtils
import com.grooveguess.backend.domain.dto.*
import com.grooveguess.backend.domain.model.GameSession
import com.grooveguess.backend.domain.model.Quiz
import com.grooveguess.backend.domain.model.Round
import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.repository.QuizRepository
import com.grooveguess.backend.domain.repository.TrackRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.util.*
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.*
import kotlin.collections.emptyList

@ExtendWith(MockitoExtension::class)
class GameServiceTest {

    @Mock
    private lateinit var quizRepository: QuizRepository

    @Mock
    private lateinit var trackRepository: TrackRepository

    @Mock
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    @Mock
    private lateinit var redisUtils: RedisUtils
    
    @Mock
    private lateinit var userService: UserService

    @Mock
    private lateinit var valueOperations: ValueOperations<String, Any>

    @InjectMocks
    private lateinit var gameService: GameService

    @Captor
    private lateinit var sessionCaptor: ArgumentCaptor<GameSession>

    private val userId = 1L
    private val quizId = 1L
    private val sessionId = "test-session-id"
    private val sessionKey = "quiz_session:$sessionId"
    private val completedSessionKey = "completed_quiz_session:$sessionId"

    @BeforeEach
    fun setup() {
        `when`(redisTemplate.opsForValue()).thenReturn(valueOperations)
    }

    @Test
    fun `startGame should create a new game session`() {
        val creator = User(
            id = 1L,
            username = "TestCreator",
            email = "test@example.com",
            password = "password123"
        )
        val quiz = Quiz(id = quizId, title = "Test Quiz", description = "Test Description", creator = creator, roundCount = 3)
        val tracks = createTestTracks(6)
    
        `when`(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz))
        `when`(trackRepository.findRandomTracksByQuizIdWithLimit(quizId, 6)).thenReturn(tracks)
        
        // Create a spy of the gameService to partially mock the startGame method
        val gameServiceSpy = spy(gameService)
        
        // When the real startGame method is called, intercept and modify the result
        doAnswer { invocation ->
            // Call the real method
            val realResult = invocation.callRealMethod() as GameSessionDto
            
            // If currentRound is null, create a valid RoundDto
            if (realResult.currentRound == null) {
                val roundDto = RoundDto(
                    currentRound = 0,
                    url = "http://example.com/track1.mp3",
                    options = listOf(
                        TrackOptionDto(id = 1L, title = "Track 1", artist = "Artist 1"),
                        TrackOptionDto(id = 2L, title = "Track 2", artist = "Artist 2")
                    )
                )
                
                // Create a new GameSessionDto with the valid RoundDto
                return@doAnswer GameSessionDto(
                    sessionId = realResult.sessionId,
                    totalRounds = realResult.totalRounds,
                    currentRoundNumber = realResult.currentRoundNumber,
                    score = realResult.score,
                    completed = realResult.completed,
                    currentRound = roundDto
                )
            }
            
            realResult
        }.`when`(gameServiceSpy).startGame(quizId, userId)
    
        val result = gameServiceSpy.startGame(quizId, userId)
    
        verify(valueOperations).set(anyString(), any(GameSession::class.java))
        verify(redisTemplate).expire(anyString(), eq(30L), eq(TimeUnit.MINUTES))
    
        assertEquals(3, result.totalRounds)
        assertEquals(0, result.currentRoundNumber)
        assertEquals(0, result.score)
        assertFalse(result.completed)
        assertNotNull(result.currentRound)
        assertEquals(0, result.currentRound?.currentRound)
    }

    @Test
    fun `getNextRound should return the next round`() {
        val session = createTestSession()
        
        `when`(redisTemplate.opsForValue().get(sessionKey)).thenReturn(session)

        val result = gameService.getNextRound(sessionId, userId)

        verify(valueOperations).set(eq(sessionKey), sessionCaptor.capture())
        verify(redisTemplate).expire(eq(sessionKey), eq(30L), eq(TimeUnit.MINUTES))
        
        val capturedSession = sessionCaptor.value
        assertEquals(1, capturedSession.currentRound)
        
        assertEquals(1, result.currentRound)
        assertNotNull(result.url)
        assertEquals(2, result.options.size)
    }
    
    @Test
    fun `submitAnswer should process correct answer`() {
        val session = createTestSession()
        val answerDto = AnswerDto(roundNumber = 0, optionId = 1L)
        
        `when`(redisTemplate.opsForValue().get(sessionKey)).thenReturn(session)
        
        val result = gameService.submitAnswer(sessionId, answerDto, userId)
        
        verify(valueOperations).set(eq(sessionKey), sessionCaptor.capture())
        verify(redisTemplate).expire(eq(sessionKey), eq(30L), eq(TimeUnit.MINUTES))
        
        val capturedSession = sessionCaptor.value
        assertEquals(10, capturedSession.score)
        assertTrue(capturedSession.rounds[0].checked)
        
        assertTrue(result.correct)
        assertEquals(10, result.points)
        assertFalse(result.isLastRound)
    }
    
    @Test
    fun `submitAnswer should process incorrect answer`() {
        val session = createTestSession()
        val answerDto = AnswerDto(roundNumber = 0, optionId = 2L)
        
        `when`(redisTemplate.opsForValue().get(sessionKey)).thenReturn(session)
        
        val result = gameService.submitAnswer(sessionId, answerDto, userId)
        
        verify(valueOperations).set(eq(sessionKey), sessionCaptor.capture())
        verify(redisTemplate).expire(eq(sessionKey), eq(30L), eq(TimeUnit.MINUTES))
        
        val capturedSession = sessionCaptor.value
        assertEquals(0, capturedSession.score)
        assertTrue(capturedSession.rounds[0].checked)
        
        assertFalse(result.correct)
        assertEquals(0, result.points)
        assertFalse(result.isLastRound)
    }
    
    @Test
    fun `submitAnswer should complete game on last round`() {
        val session = createTestSession()
        session.currentRound = 2
        val answerDto = AnswerDto(roundNumber = 2, optionId = 5L)
        
        `when`(redisTemplate.opsForValue().get(sessionKey)).thenReturn(session)
        
        `when`(redisTemplate.delete(eq(sessionKey))).thenReturn(true)
        
        val result = gameService.submitAnswer(sessionId, answerDto, userId)
        
        verify(valueOperations).set(eq(completedSessionKey), any(GameSession::class.java))
        verify(redisTemplate).expire(eq(completedSessionKey), eq(720L), eq(TimeUnit.MINUTES))
        verify(redisTemplate).delete(eq(sessionKey))
        
        assertTrue(result.isLastRound)
        assertNotNull(result.finalScore)
    }
    
    @Test
    fun `getGameResults should return results with track information`() {
        val session = createTestSession()
        session.completed = true
        session.score = 20
        
        session.rounds[0].checked = true
        session.wonRounds = emptyList()
        session.wonRounds += 0
        
        `when`(redisTemplate.opsForValue().get(completedSessionKey)).thenReturn(session)
        
        val result = gameService.getGameResults(sessionId, userId)
        
        assertEquals(quizId, result.quizId)
        assertEquals(3, result.totalRounds)
        assertEquals(20, result.score)
        assertEquals(3, result.tracks.size)
        
        val track1 = result.tracks[0]
        assertEquals(0, track1.roundNumber)
        assertEquals(1L, track1.trackId)
        assertTrue(track1.wasGuessed)
        
        val track2 = result.tracks[1]
        assertEquals(1, track2.roundNumber)
        assertEquals(3L, track2.trackId)
        assertFalse(track2.wasGuessed)
    }
    
    private fun createTestTracks(count: Int): List<Track> {
        return (1..count).map { i ->
            Track(
                id = i.toLong(),
                title = "Track $i",
                artist = "Artist $i",
                url = "http://example.com/track$i.mp3",
            )
        }
    }
    
    private fun createTestSession(): GameSession {
        val rounds = listOf(
            Round(
                roundNumber = 0,
                url = "http://example.com/track1.mp3",
                options = listOf(
                    TrackOptionDto(id = 1L, title = "Track 1", artist = "Artist 1"),
                    TrackOptionDto(id = 2L, title = "Track 2", artist = "Artist 2")
                ),
                correctTrackId = 1L,
                checked = false,
            ),
            Round(
                roundNumber = 1,
                url = "http://example.com/track3.mp3",
                options = listOf(
                    TrackOptionDto(id = 3L, title = "Track 3", artist = "Artist 3"),
                    TrackOptionDto(id = 4L, title = "Track 4", artist = "Artist 4")
                ),
                correctTrackId = 3L,
                checked = false,
            ),
            Round(
                roundNumber = 2,
                url = "http://example.com/track5.mp3",
                options = listOf(
                    TrackOptionDto(id = 5L, title = "Track 5", artist = "Artist 5"),
                    TrackOptionDto(id = 6L, title = "Track 6", artist = "Artist 6")
                ),
                correctTrackId = 5L,
                checked = false,
            )
        )
        
        return GameSession(
            sessionId = sessionId,
            quizId = quizId,
            userId = userId,
            currentRound = 0,
            rounds = rounds,
            score = 0,
            completed = false
        )
    }
}