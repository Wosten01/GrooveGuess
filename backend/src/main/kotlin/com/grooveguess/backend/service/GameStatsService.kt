package com.grooveguess.backend.service

import com.grooveguess.backend.config.RedisUtils
import com.grooveguess.backend.domain.dto.*
import com.grooveguess.backend.domain.model.GameSession
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.grooveguess.backend.service.GameService

@Service
class GameStatsService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val redisUtils: RedisUtils,
    private val userService: UserService,
    private val quizService: QuizService,
    private val gameService: GameService
) {
    private val logger = LoggerFactory.getLogger(GameStatsService::class.java)
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    
    companion object {
        private const val COMPLETED_SESSION_PREFIX = "completed_quiz_session:"
    }
    
    fun getRecentGamesPaginated(page: Int, size: Int, userId: Long? = null): Page<RecentGameDto> {
        logger.debug("Getting paginated recent games, page: $page, size: $size, userId: $userId")
        
        val pattern = "$COMPLETED_SESSION_PREFIX*"
        val keys = redisTemplate.keys(pattern)
        
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

    fun exportPlayerStats(userId: Long, format: String = "json", gamesLimit: Int = 100): ResponseEntity<Resource> {
        logger.debug("Exporting player stats for user: $userId in format: $format")
        
        val user = userService.findById(userId)
        if (user == null) {
            logger.error("User not found with ID: $userId")
            throw IllegalArgumentException("User not found")
        }
        
        val overallStats = getUserStats(userId)
        
        val recentGames = getRecentGames(gamesLimit, userId)
        
        val detailedGames = recentGames.mapNotNull { recentGame ->
            try {
                val sessionId = recentGame.sessionId
                
                val gameResults = gameService.getGameResults(sessionId, userId)
                
                mapOf(
                    "sessionId" to sessionId,
                    "timestamp" to recentGame.timestamp,
                    "formattedDate" to formatTimestamp(recentGame.timestamp),
                    "gameResults" to gameResults
                )
            } catch (e: Exception) {
                logger.error("Error retrieving detailed game results for session ${recentGame.sessionId}: ${e.message}", e)
                null
            }
        }
        
        val exportData = mapOf(
            "user" to mapOf(
                "username" to user.username,
                "email" to user.email,
            ),
            "overallStats" to overallStats,
            "recentGames" to recentGames,
            "detailedGames" to detailedGames
        )
        
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val filename = "player_stats_${user.username}_$timestamp.$format"
        
        val contentType: MediaType
        val fileContent: ByteArray
        
        when (format.lowercase()) {
            "json" -> {
                contentType = MediaType.APPLICATION_JSON
                fileContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(exportData)
            }
            "csv" -> {
                contentType = MediaType.parseMediaType("text/csv")
                fileContent = generateCsvContent(exportData)
            }
            else -> {
                logger.error("Unsupported export format: $format")
                throw IllegalArgumentException("Unsupported export format: $format. Supported formats are: json, csv")
            }
        }
        
        val resource = ByteArrayResource(fileContent)
        
        return ResponseEntity.ok()
            .contentType(contentType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .body(resource)
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        return try {
            val dateTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC)
            dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        } catch (e: Exception) {
            "Invalid Date"
        }
    }
    
    private fun generateCsvContent(exportData: Map<String, Any>): ByteArray {
    val stringBuilder = StringBuilder()
    
    val user = exportData["user"] as Map<*, *>
    stringBuilder.appendLine("GrooveGuess Player Statistics")
    stringBuilder.appendLine("Username: ${user["username"]}")
    stringBuilder.appendLine("Email: ${user["email"]}")
    stringBuilder.appendLine("Export Date: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
    stringBuilder.appendLine()
    
    val stats = exportData["overallStats"] as GameStatsDto
    stringBuilder.appendLine("Overall Statistics")
    stringBuilder.appendLine("Total Games,Total Score,Average Score,Highest Score,Accuracy (%)")
    stringBuilder.appendLine("${stats.totalGames},${stats.totalScore},${stats.averageScore},${stats.highestScore},${stats.accuracy}")
    stringBuilder.appendLine()
    
    val recentGames = exportData["recentGames"] as List<*>
    stringBuilder.appendLine("Game Sessions:")
    stringBuilder.appendLine("Session ID,Quiz Title,Score,Total Rounds,Correct Answers,Accuracy (%)")
    
    recentGames.forEach { game ->
        game as RecentGameDto
        val accuracy = if (game.totalRounds > 0) (game.correctAnswers * 100) / game.totalRounds else 0
        stringBuilder.appendLine("${game.sessionId},\"${game.quizTitle}\",${game.score},${game.totalRounds},${game.correctAnswers},${accuracy}")
    }
    stringBuilder.appendLine()
    
    val detailedGames = exportData["detailedGames"] as List<*>
    
    detailedGames.forEachIndexed { _, gameData ->
        gameData as Map<*, *>
        val gameResults = gameData["gameResults"] as GameResultsDto
        
        stringBuilder.appendLine("Session Details: ${gameData["sessionId"]}")
        stringBuilder.appendLine("Quiz: \"${gameResults.quizTitle}\"")
        stringBuilder.appendLine("Date: ${gameData["formattedDate"]}")
        stringBuilder.appendLine("Score: ${gameResults.score}")
        stringBuilder.appendLine()
        
        gameResults.tracks.forEach { track ->
            stringBuilder.appendLine("Round ${track.roundNumber + 1}:")
            
            val correctOption = track.options.find { 
                it.title == track.title && it.artist == track.artist 
            }
            
            stringBuilder.appendLine("Title,Artist,User Selected,Correct Option")
            
            track.options.forEach { option ->
                val isSelected = track.userAnswer?.selectedOptionId == option.id
                val isCorrect = option.id == correctOption?.id
                
                val selectedMark = if (isSelected) "X" else ""
                val correctMark = if (isCorrect) "✓" else ""
                
                stringBuilder.appendLine("${option.title}\",\"${option.artist}\",${selectedMark},${correctMark}")
            }
            stringBuilder.appendLine()
        }
        
        stringBuilder.appendLine("------------------------------")
        stringBuilder.appendLine()
    }
    
    return stringBuilder.toString().toByteArray()
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