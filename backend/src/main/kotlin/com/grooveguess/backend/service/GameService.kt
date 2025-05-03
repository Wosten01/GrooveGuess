package com.grooveguess.backend.service

import com.grooveguess.backend.domain.dto.*
import com.grooveguess.backend.domain.repository.QuizRepository
import com.grooveguess.backend.domain.repository.TrackRepository
import com.grooveguess.backend.domain.model.Quiz
import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.model.GameSession
import com.grooveguess.backend.domain.model.Round
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.TimeUnit
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.beans.factory.annotation.Qualifier
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlin.math.round
import org.slf4j.LoggerFactory
import com.grooveguess.backend.config.RedisUtils
import com.grooveguess.backend.exception.AccessDeniedException

@Service
class GameService(
    private val quizRepository: QuizRepository,
    private val trackRepository: TrackRepository,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val redisUtils: RedisUtils,
    private val userService: UserService,
    private val quizService: QuizService,
) {
    private val logger = LoggerFactory.getLogger(GameService::class.java)

    companion object {
        private const val SESSION_PREFIX = "quiz_session:"
        private const val SESSION_TTL_MINUTES = 30L
        private const val COMPLETED_SESSION_PREFIX = "completed_quiz_session:"
        private const val COMPLETED_SESSION_TTL_MINUTES = 30L * 2 * 12
    }

    fun startGame(quizId: Long, userId: Long): GameSessionDto {
        logger.debug("Starting game for quiz $quizId and user $userId")
        val quiz = quizRepository.findById(quizId).orElseThrow { 
            IllegalArgumentException("Quiz not found with id: $quizId") 
        }
        
        val sessionId = UUID.randomUUID().toString()
        val sessionKey = "$SESSION_PREFIX$sessionId"
        
        // TODO: Added TracksPerRound
        val optionsPerRound = 2

        val limit = quiz.roundCount * optionsPerRound.toLong()
        
        val tracks = getTracksForQuiz(quizId, limit)
        val shuffledTracks = tracks.shuffled()
        
        val trackDTOs = shuffledTracks.map { track ->
            TrackDto(
                id = track.id,
                title = track.title,
                artist = track.artist,
                url = track.url
            )
        }

        val rounds = createRounds(trackDTOs, quiz.roundCount, optionsPerRound)
        
        val session = GameSession(
            sessionId = sessionId,
            quizId = quizId,
            userId = userId,
            currentRound = 0,
            rounds = rounds,
            score = 0,
        )
        
        logger.debug("Saving session to Redis: $sessionKey")
        val opsForValue = redisTemplate.opsForValue()
        opsForValue.set(sessionKey, session)
        redisTemplate.expire(sessionKey, SESSION_TTL_MINUTES, TimeUnit.MINUTES)
        logger.debug("Session saved to Redis: $sessionKey")
        
        return GameSessionDto(
            sessionId = sessionId,
            totalRounds = rounds.size,
            currentRoundNumber = 0,
            score = 0,
            completed = false,
        )
    }

    fun getNextRound(sessionId: String, userId: Long): RoundDto {
        logger.debug("Getting next round for session $sessionId")
        
        val session = getSession(sessionId, userId)
        
        if (session.completed) {
            logger.debug("Game is already completed for session $sessionId")
            throw IllegalStateException("Game is already completed")
        }
        
        if (session.currentRound >= session.rounds.size - 1) {
            logger.debug("No more rounds available for session $sessionId")
            throw IllegalStateException("No more rounds available")
        }
        
        session.currentRound++
        
        if (session.currentRound < 0 || session.currentRound >= session.rounds.size) {
            logger.warn("Invalid round number after increment: ${session.currentRound}")
            session.currentRound--
            throw IllegalStateException("Invalid round number after increment")
        }
        
        val nextRound = session.rounds[session.currentRound]

        updateSession(session)
        
        return RoundDto(
            currentRound = session.currentRound,
            url = nextRound.url,
            options = nextRound.options,
        )
    }

    fun getCurrentRound(sessionId: String, userId: Long): GameSessionDto {
        logger.debug("Getting current round for session $sessionId")
        
        val session = getSession(sessionId, userId)
        
        if (session.completed) {
            logger.debug("Game is already completed for session $sessionId")
            throw IllegalStateException("Game is already completed")
        }
        
        if (session.currentRound < 0 || session.currentRound >= session.rounds.size) {
            logger.warn("Invalid current round number: ${session.currentRound}")
            throw IllegalStateException("Invalid current round number")
        }
        
        val currentRound = session.rounds[session.currentRound]


        return GameSessionDto(
            sessionId = sessionId,
            totalRounds = session.rounds.size,
            currentRoundNumber = session.currentRound,
            score = session.score,
            completed = false,
            currentRound = RoundDto(
                currentRound = session.currentRound,
                url = currentRound.url,
                options = currentRound.options
            ),
        )
    
    }

    fun submitAnswer(sessionId: String, answer: AnswerDto, userId : Long): AnswerResultDto {
        logger.debug("Submitting answer for session $sessionId, round ${answer.roundNumber}, option ${answer.optionId}")
        val session = getSession(sessionId, userId)
        
        if (session.completed) {
            logger.debug("Game is already completed for session $sessionId")
            throw IllegalStateException("Game is already completed")
        }
        
        val roundIndex = answer.roundNumber
        if (roundIndex < 0 || roundIndex >= session.rounds.size) {
            logger.warn("Invalid round number: ${answer.roundNumber}")
            throw IllegalArgumentException("Invalid round number")
        }
    
        if (session.currentRound != answer.roundNumber){
            logger.warn("Illegal round number: ${answer.roundNumber}")
            throw IllegalArgumentException("Illegal round number")
        }
        
        val round = session.rounds[roundIndex]
    
        if (round.checked) {
            logger.warn("Answer was already received previously (Session:$sessionId, round:${answer.roundNumber})")
            throw IllegalArgumentException("Answer was already received previously")
        }
    
        val isCorrect = answer.optionId == round.correctTrackId
        
        val points = if (isCorrect) 10 else 0
    
        if (isCorrect) {
            session.score += points
            session.wonRounds += roundIndex
        }

        val userAnswer = UserAnswerDto(
            roundNumber = answer.roundNumber,
            selectedOptionId = answer.optionId,
            isCorrect = isCorrect
        )

        session.rounds[roundIndex].checked = true

        session.userAnswers += (userAnswer)

        val isLastRound = session.currentRound >= session.rounds.size - 1
        
        if (isLastRound) {
            completeGame(session)
            userService.addScore(userId, session.score)
        } else {
            updateSession(session)
        }
        
        return AnswerResultDto(
            correct = isCorrect,
            points = points,
            isLastRound = isLastRound,
            finalScore = if (isLastRound) session.score else 0
        )
    }

    fun completeGame(session: GameSession) {
        logger.debug("Completing game for session ${session.sessionId}")
        
        session.completed = true

        session.timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        
        val activeSessionKey = "$SESSION_PREFIX${session.sessionId}"
        val completedSessionKey = "$COMPLETED_SESSION_PREFIX${session.sessionId}"
        
        redisTemplate.opsForValue().set(completedSessionKey, session)
        redisTemplate.expire(completedSessionKey, COMPLETED_SESSION_TTL_MINUTES, TimeUnit.MINUTES)
        
        redisTemplate.delete(activeSessionKey)
        
        logger.debug("Game completed for session ${session.sessionId} with score ${session.score}")
    }

    fun getGameResults(sessionId: String, userId: Long): GameResultsDto {
        logger.debug("Getting game results for session $sessionId")
        
        val completedSessionKey = "$COMPLETED_SESSION_PREFIX$sessionId"
        var rawData = redisTemplate.opsForValue().get(completedSessionKey)
        
        if (rawData == null) {
            val activeSessionKey = "$SESSION_PREFIX$sessionId"
            rawData = redisTemplate.opsForValue().get(activeSessionKey)
        }
        
        if (rawData == null) {
            logger.warn("Session not found: $sessionId")
            throw IllegalStateException("Session not found")
        }
        
        val session = when (rawData) {
            is GameSession -> rawData
            is Map<*, *> -> {
                try {
                    redisUtils.convertMapToObject(rawData, GameSession::class.java)
                } catch (e: Exception) {
                    logger.error("Error converting session data: ${e.message}", e)
                    throw IllegalStateException("Failed to convert session data: ${e.message}")
                }
            }
            else -> throw IllegalStateException("Unexpected data type: ${rawData.javaClass.name}")
        }
        
        if (session.userId != userId) {
            logger.warn("User $userId does not own session")
            throw IllegalAccessException("You don't have access to this session")
        }
        
        if (!session.completed && !isGameFinished(session)) {
            logger.warn("Session $sessionId is not completed")
            throw IllegalAccessException("Session is not completed")
        }
        
        if (!session.completed && isGameFinished(session)) {
            completeGame(session)
        }

        val userAnswersMap = session.userAnswers.associateBy { it.roundNumber }

        val trackResults = session.rounds.mapIndexed { _, round ->
            val roundNumber = round.roundNumber
            val wasGuessed = session.wonRounds.contains(roundNumber)
            val userAnswer = userAnswersMap[roundNumber]
            
            TrackResultDto(
                roundNumber = roundNumber,
                trackId = round.correctTrackId,
                title = round.options.find { it.id == round.correctTrackId }?.title ?: "Unknown",
                artist = round.options.find { it.id == round.correctTrackId }?.artist ?: "Unknown",
                url = round.url,
                wasGuessed = wasGuessed,
                options = round.options,
                userAnswer = userAnswer
            )
        }

        val quiz = quizService.find(session.quizId)
        
        return GameResultsDto(
            quizId = session.quizId,
            totalRounds = session.rounds.size,
            score = session.score,
            tracks = trackResults,
            userAnswers = session.userAnswers,
            quizTitle = quiz.title
        )
    }

    private fun isGameFinished(session: GameSession): Boolean {
        return session.rounds.all { it.checked }
    }

    private fun getTracksForQuiz(quizId: Long, limit: Long): List<Track> {
        return trackRepository.findRandomTracksByQuizIdWithLimit(quizId, limit)
    }

    private fun createRounds(
        tracks: List<TrackDto>, 
        roundCount: Int, 
        optionsPerRound: Int
    ): List<Round> {
        val rounds = mutableListOf<Round>()
        
        if (tracks.size < roundCount * optionsPerRound) {
            throw IllegalArgumentException(
                "Not enough tracks available. Need ${roundCount * optionsPerRound}, but only have ${tracks.size}"
            )
        }
        
        val shuffledTracks = tracks.shuffled()
        
        for (roundIndex in 0 until roundCount) {
            val startIndex = roundIndex * optionsPerRound
            
            val tracksForRound = shuffledTracks.subList(startIndex, startIndex + optionsPerRound)
            val correctTrack = tracksForRound.first()
            val options = generateOptions(tracksForRound)
            
            rounds.add(
                Round(
                    roundNumber = roundIndex,
                    url = correctTrack.url,
                    options = options,
                    correctTrackId = correctTrack.id,
                    checked = false,
                )
            )
        }
        
        return rounds
    }

    private fun generateOptions(tracks: List<TrackDto>): List<TrackOptionDto> {
        return tracks.map { track ->
            TrackOptionDto(
                id = track.id,
                title = track.title,
                artist = track.artist
            )
        }.shuffled()
    }

    private fun getSession(sessionId: String, userId: Long): GameSession {
        val sessionKey = "$SESSION_PREFIX$sessionId"
        logger.debug("Retrieving session from Redis: $sessionKey")
        
        val rawData = redisTemplate.opsForValue().get(sessionKey)
        
        if (rawData == null) {
            // Try to get from completed sessions
            val completedSessionKey = "$COMPLETED_SESSION_PREFIX$sessionId"
            val completedData = redisTemplate.opsForValue().get(completedSessionKey)
            
            if (completedData == null) {
                logger.warn("Session not found: $sessionId")
                throw IllegalStateException("Session not found")
            }
            
            val completedSession = when (completedData) {
                is GameSession -> completedData
                is Map<*, *> -> {
                    try {
                        redisUtils.convertMapToObject(completedData, GameSession::class.java)
                    } catch (e: Exception) {
                        logger.error("Error converting completed session data: ${e.message}", e)
                        throw IllegalStateException("Failed to convert completed session data: ${e.message}")
                    }
                }
                else -> throw IllegalStateException("Unexpected data type for completed session: ${completedData.javaClass.name}")
            }
            
            if (completedSession.userId != userId) {
                logger.warn("User $userId does not own completed session")
                throw AccessDeniedException("User has no access to this completed session")
            }
            
            return completedSession
        }
        
        val session = when (rawData) {
            is GameSession -> rawData
            is Map<*, *> -> {
                try {
                    val session = redisUtils.convertMapToObject(rawData, GameSession::class.java)
                    logger.debug("Converted session: $session")
                    session
                } catch (e: Exception) {
                    logger.error("Error converting session data: ${e.message}", e)
                    throw IllegalStateException("Failed to convert session data: ${e.message}")
                }
            }
            else -> throw IllegalStateException("Unexpected data type: ${rawData.javaClass.name}")
        }

        if (session.userId != userId) {
            logger.warn("User $userId does not own session")
            throw AccessDeniedException("You don't have access to this session")
        }
        
        return session
    }

    private fun updateSession(session: GameSession) {
        val sessionKey = "$SESSION_PREFIX${session.sessionId}"
        logger.debug("Updating session in Redis: $sessionKey")
        redisTemplate.opsForValue().set(sessionKey, session)
        redisTemplate.expire(sessionKey, SESSION_TTL_MINUTES, TimeUnit.MINUTES)
        logger.debug("Session updated in Redis: $sessionKey")
    }
}