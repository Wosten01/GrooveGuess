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

@Service
class QuizGameService(
    private val quizRepository: QuizRepository,
    private val trackRepository: TrackRepository,
    private val redisTemplate: RedisTemplate<String, Any>
) {
    companion object {
        private const val SESSION_PREFIX = "quiz_session:"
        private const val SESSION_TTL_HOURS = 0.5.toLong() 
    }

    fun startGame(quizId: Long, userId: Long): QuizGameSessionDto {
        val quiz = quizRepository.findById(quizId).orElseThrow { 
            IllegalArgumentException("Quiz not found with id: $quizId") 
        }
        
        val sessionId = UUID.randomUUID().toString()
        val sessionKey = "$SESSION_PREFIX$sessionId"
        
        val tracks = getTracksForQuiz(quiz)
        val shuffledTracks = tracks.shuffled()
        
        val session = GameSession(
            sessionId = sessionId,
            quizId = quizId,
            userId = userId,
            tracks = shuffledTracks,
            currentRound = 0,
            score = 0,
            answeredRounds = mutableMapOf()
        )
        
        val opsForValue = redisTemplate.opsForValue()
        opsForValue.set(sessionKey, session)
        redisTemplate.expire(sessionKey, SESSION_TTL_HOURS, TimeUnit.HOURS)
        
        return QuizGameSessionDto(
            sessionId = sessionId,
            totalRounds = shuffledTracks.size,
            currentScore = 0
        )
    }

    fun getNextRound(quizId: Long, userId: Long): QuizRoundDto? {
        val session = getSession(quizId, userId) ?: return null
        
        // Если все раунды пройдены, возвращаем null
        if (session.currentRound >= session.tracks.size) {
            return null
        }
        
        // Получаем текущий трек и увеличиваем номер раунда
        val currentTrack = session.tracks[session.currentRound]
        val roundNumber = session.currentRound + 1
        
        // Генерируем варианты ответов
        val options = generateOptions(currentTrack, session.tracks)
        
        // Обновляем сессию
        session.currentRound++
        updateSession(session)
        
        return QuizRoundDto(
            roundNumber = roundNumber,
            totalRounds = session.tracks.size,
            audioUrl = currentTrack.url,
            options = options
        )
    }

    fun submitAnswer(quizId: Long, userId: Long, answer: AnswerDto): AnswerResultDto {
        val session = getSession(quizId, userId) ?: throw IllegalStateException("Session not found")
        
        // Проверяем, что ответ на правильный раунд
        val roundIndex = answer.roundNumber - 1
        if (roundIndex < 0 || roundIndex >= session.tracks.size) {
            throw IllegalArgumentException("Invalid round number")
        }
        
        // Проверяем, что на этот раунд еще не отвечали
        if (session.answeredRounds.containsKey(roundIndex)) {
            throw IllegalArgumentException("Round already answered")
        }
        
        val track = session.tracks[roundIndex]
        val correctOptionId = track.id
        val isCorrect = answer.optionId == correctOptionId
        
        // Начисляем очки за правильный ответ
        val points = if (isCorrect) 10 else 0
        if (isCorrect) {
            session.score += points
        }
        
        // Отмечаем раунд как отвеченный
        session.answeredRounds[roundIndex] = isCorrect
        
        // Обновляем сессию
        updateSession(session)
        
        return AnswerResultDto(
            correct = isCorrect,
            points = points,
            isLastRound = session.currentRound >= session.tracks.size
        )
    }

    /**
     * Получает треки для квиза
     */
    private fun getTracksForQuiz(quiz: Quiz): List<Track> {
        // Здесь должна быть логика получения ��реков для квиза
        // Например, через связь Quiz -> Track или отдельную таблицу
        return trackRepository.findByQuizId(quiz.id)
    }

    /**
     * Генерирует варианты ответов для раунда
     */
    private fun generateOptions(correctTrack: Track, allTracks: List<Track>): List<OptionDto> {
        val options = mutableListOf<OptionDto>()
        
        // Добавляем правильный вариант
        options.add(OptionDto(
            id = correctTrack.id,
            title = correctTrack.title
        ))
        
        // Добавляем случайные неправильные варианты
        val wrongOptions = allTracks
            .filter { it.id != correctTrack.id }
            .shuffled()
            .take(3) // Берем 3 неправильных варианта
        
        wrongOptions.forEach { track ->
            options.add(OptionDto(
                id = track.id,
                title = track.title
            ))
        }
        
        // Перемешиваем варианты
        return options.shuffled()
    }

    /**
     * Получает сессию из Redis
     */
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
        redisTemplate.opsForValue().set(sessionKey, session)
        redisTemplate.expire(sessionKey, SESSION_TTL_HOURS, TimeUnit.HOURS)
    }

    data class GameSession(
        val sessionId: String,
        val quizId: Long,
        val userId: Long,
        val tracks: List<Track>,
        var currentRound: Int,
        var score: Int,
        val answeredRounds: MutableMap<Int, Boolean>
    )
}