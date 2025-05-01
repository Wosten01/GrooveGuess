package com.grooveguess.backend.service

import com.grooveguess.backend.domain.dto.*
import com.grooveguess.backend.domain.repository.QuizRepository
import com.grooveguess.backend.domain.repository.TrackRepository
import com.grooveguess.backend.domain.model.Quiz
import com.grooveguess.backend.domain.model.Track
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit
import org.springframework.data.redis.core.RedisTemplate
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import java.io.Serializable
import kotlin.math.round
import org.slf4j.LoggerFactory

@Service
class QuizGameService(
    private val quizRepository: QuizRepository,
    private val trackRepository: TrackRepository,
    private val redisTemplate: RedisTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(QuizGameService::class.java)

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "sessionId")
    data class GameSession(
        val sessionId: String,
        val quizId: Long,
        val userId: Long,
        val rounds: List<Round>,
        var currentRound: Int,
        var score: Int,
        val answeredRounds: MutableMap<Int, Boolean> = mutableMapOf()
    ) : Serializable

    data class Round(
        val roundNumber: Int,
        val audioUrl: String,
        val options: List<TrackOptionDto>,
        val correctTrackId: Long
    ): Serializable

    data class TrackDto(
        val id: Long,
        val title: String,
        val artist: String,
        val url: String
    ) : Serializable

    companion object {
        private const val SESSION_PREFIX = "quiz_session:"
        private const val SESSION_TTL_MINUTES = 30L
    }

    fun startGame(quizId: Long, userId: Long): GameSessionDto {
        logger.info("Starting game for quiz $quizId and user $userId")
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
            currentRound = 0,
            score = 0
        )
    }

    // fun getNextRound(sessionId: String): RoundDto? {
    //     logger.info("Getting next round for session $sessionId")
    //     val sessionKey = "$SESSION_PREFIX$sessionId"
    //     val session = redisTemplate.opsForValue().get(sessionKey) as? GameSession ?: run {
    //         logger.warn("Session not found: $sessionKey")
    //         return null
    //     }
        
    //     if (session.currentRound >= session.rounds.size) {
    //         logger.info("No more rounds available for session $sessionId")
    //         return null
    //     }
        
    //     val round = session.rounds[session.currentRound]
    //     session.currentRound++
        
    //     updateSession(session)
        
    //     return RoundDto(
    //         roundNumber = round.roundNumber,
    //         totalRounds = session.rounds.size,
    //         audioUrl = round.audioUrl,
    //         options = round.options
    //     )
    // }

    // fun submitAnswer(sessionId: String, answer: AnswerDto): AnswerResultDto {
    //     logger.info("Submitting answer for session $sessionId, round ${answer.roundNumber}, option ${answer.optionId}")
    //     val sessionKey = "$SESSION_PREFIX$sessionId"
    //     val session = redisTemplate.opsForValue().get(sessionKey) as? GameSession ?: run {
    //         logger.warn("Session not found: $sessionKey")
    //         throw IllegalStateException("Session not found")
    //     }
        
    //     val roundIndex = answer.roundNumber - 1
    //     if (roundIndex < 0 || roundIndex >= session.rounds.size) {
    //         logger.warn("Invalid round number: ${answer.roundNumber}")
    //         throw IllegalArgumentException("Invalid round number")
    //     }
        
    //     if (session.answeredRounds.containsKey(roundIndex)) {
    //         logger.warn("Round already answered: ${answer.roundNumber}")
    //         throw IllegalArgumentException("Round already answered")
    //     }
        
    //     val round = session.rounds[roundIndex]
    //     val correctOptionId = round.correctTrackId
    //     val isCorrect = answer.optionId == correctOptionId
        
    //     val points = if (isCorrect) 10 else 0
    //     if (isCorrect) {
    //         session.score += points
    //     }
        
    //     session.answeredRounds[roundIndex] = isCorrect
        
    //     updateSession(session)
        
    //     return AnswerResultDto(
    //         correct = isCorrect,
    //         points = points,
    //         isLastRound = session.currentRound >= session.rounds.size,
    //         currentScore = session.score
    //     )
    // }

    // fun getGameStatus(sessionId: String): GameSessionDto? {
    //     logger.info("Getting game status for session $sessionId")
    //     val sessionKey = "$SESSION_PREFIX$sessionId"
    //     val session = redisTemplate.opsForValue().get(sessionKey) as? GameSession ?: run {
    //         logger.warn("Session not found: $sessionKey")
    //         return null
    //     }
        
    //     return GameSessionDto(
    //         sessionId = session.sessionId,
    //         totalRounds = session.rounds.size,
    //         currentRound = session.currentRound,
    //         score = session.score
    //     )
    // }

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
                    roundNumber = roundIndex + 1,
                    audioUrl = correctTrack.url,
                    options = options,
                    correctTrackId = correctTrack.id
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

    private fun getSession(quizId: Long, userId: Long): GameSession? {
        val sessionKeys = redisTemplate.keys("$SESSION_PREFIX*")
        
        for (key in sessionKeys) {
            val session = redisTemplate.opsForValue().get(key) as? GameSession ?: continue
            
            if (session.quizId == quizId && session.userId == userId) {
                return session
            }
        }
        
        return null
    }

    private fun updateSession(session: GameSession) {
        val sessionKey = "$SESSION_PREFIX${session.sessionId}"
        logger.debug("Updating session in Redis: $sessionKey")
        redisTemplate.opsForValue().set(sessionKey, session)
        // redisTemplate.expire(sessionKey, SESSION_TTL_HOURS, TimeUnit.HOURS)
        logger.debug("Session updated in Redis: $sessionKey")
    }
}