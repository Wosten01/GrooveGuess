package com.grooveguess.backend.service

import com.grooveguess.backend.config.RedisUtils
import com.grooveguess.backend.domain.dto.GameStatsDto
import com.grooveguess.backend.domain.dto.RecentGameDto
import com.grooveguess.backend.domain.model.GameSession
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class GameStatsService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val redisUtils: RedisUtils,
    private val userService: UserService,
    private val quizService: QuizService,
) {
    private val logger = LoggerFactory.getLogger(GameStatsService::class.java)
    
    companion object {
        private const val COMPLETED_SESSION_PREFIX = "completed_quiz_session:"
    }
    
    fun getRecentGamesPaginated(page: Int, size: Int, userId: Long? = null): Page<RecentGameDto> {
        logger.debug("Getting paginated recent games, page: $page, size: $size, userId: $userId")
        
        val pattern = "$COMPLETED_SESSION_PREFIX*"
        val keys = redisTemplate.keys(pattern) ?: emptySet()
        
        logger.debug("Found ${keys.size} completed game sessions")
        
        val allGames = keys
            .asSequence()
            .mapNotNull { key -> 
                try {
                    val rawData = redisTemplate.opsForValue().get(key)
                    convertToGameSession(rawData)
                } catch (e: Exception) {
                    logger.error("Error retrieving game session for key $key: ${e.message}", e)
                    null
                }
            }
            .filter { session -> userId == null || session.userId == userId }
            .sortedByDescending {  it.timestamp  }
            .map { session -> convertToRecentGameDto(session) }
            .toList()
        
        val totalElements = allGames.size.toLong()
        val start = page * size
        val end = minOf(start + size, allGames.size)
        
        val pageContent = if (start < totalElements) allGames.subList(start, end) else emptyList()
        
        val pageable = PageRequest.of(page, size)
        return PageImpl(pageContent, pageable, totalElements)
    }
    
    fun getRecentGames(limit: Int, userId: Long? = null): List<RecentGameDto> {
        logger.debug("Getting recent games, limit: $limit, userId: $userId")
        
        val page = getRecentGamesPaginated(0, limit, userId)
        return page.content
    }
    
    fun getUserStats(userId: Long): GameStatsDto {
        logger.debug("Getting game stats for user: $userId")
        
        val recentGames = getRecentGames(100, userId)
        
        val totalGames = recentGames.size
        val totalScore = recentGames.sumOf { it.score }
        val averageScore = if (totalGames > 0) totalScore / totalGames else 0
        val highestScore = recentGames.maxOfOrNull { it.score } ?: 0
        
        val correctAnswers = recentGames.sumOf { it.correctAnswers }
        val totalQuestions = recentGames.sumOf { it.totalRounds }
        val accuracy = if (totalQuestions > 0) (correctAnswers * 100) / totalQuestions else 0
        
        return GameStatsDto(
            totalGames = totalGames,
            totalScore = totalScore,
            averageScore = averageScore,
            highestScore = highestScore,
            accuracy = accuracy
        )
    }
    
    private fun convertToGameSession(rawData: Any?): GameSession? {
        if (rawData == null) return null
        
        return when (rawData) {
            is GameSession -> rawData
            is Map<*, *> -> {
                try {
                    redisUtils.convertMapToObject(rawData, GameSession::class.java)
                } catch (e: Exception) {
                    logger.error("Error converting session data: ${e.message}", e)
                    null
                }
            }
            else -> {
                logger.error("Unexpected data type: ${rawData.javaClass.name}")
                null
            }
        }
    }
    
    private fun convertToRecentGameDto(session: GameSession): RecentGameDto {
        val user = userService.findById(session.userId)
        val username = user?.username ?: "Unknown User"
        
        val correctAnswers = session.wonRounds.size

        val quiz = quizService.find(session.quizId)
        
        return RecentGameDto(
            quizTitle = quiz.title,
            sessionId = session.sessionId,
            userId = session.userId,
            username = username,
            quizId = session.quizId,
            score = session.score,
            totalRounds = session.rounds.size,
            correctAnswers = correctAnswers,
            timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) 
        )
    }
}