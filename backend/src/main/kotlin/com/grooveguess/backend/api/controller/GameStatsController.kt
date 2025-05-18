package com.grooveguess.backend.api.controller

import com.grooveguess.backend.domain.dto.GameStatsDto
import com.grooveguess.backend.domain.dto.RecentGameDto
import com.grooveguess.backend.domain.dto.PaginatedStatsResponseDto
import com.grooveguess.backend.service.GameStatsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.core.io.Resource
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus

@RestController
@RequestMapping("/api/game-stats")
class GameStatsController(private val gameStatsService: GameStatsService) {
    
    private val logger = LoggerFactory.getLogger(GameStatsController::class.java)
    
    @GetMapping("/recent")
    fun getRecentGames(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = true) userId: Long
    ): ResponseEntity<PaginatedStatsResponseDto> {
        logger.debug("Getting recent games, page: $page, size: $size, userId: $userId")
        
        val paginatedGames = gameStatsService.getRecentGamesPaginated(page, size, userId)
        
        logger.debug("Getting game stats for user: $userId")
        val stats = gameStatsService.getUserStats(userId)
        
        val response = PaginatedStatsResponseDto(
            games = paginatedGames.content,
            totalGames = paginatedGames.totalElements,
            totalPages = paginatedGames.totalPages,
            currentPage = paginatedGames.number,
            stats = stats
        )
        
        return ResponseEntity.ok(response)
    }

    @GetMapping("/export")
    fun exportPlayerStats(
        @RequestParam(required = true) userId: Long,
        @RequestParam(defaultValue = "json") format: String,
        @RequestParam(defaultValue = "100") gamesLimit: Int
    ): ResponseEntity<Resource> {
        logger.debug("Export request received with userId: $userId, format: $format")
        
        return try {
            gameStatsService.exportPlayerStats(userId, format, gamesLimit)
        } catch (e: IllegalArgumentException) {
            logger.error("Error exporting player stats: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(null)
        } catch (e: Exception) {
            logger.error("Unexpected error exporting player stats: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null)
        }
    }
}