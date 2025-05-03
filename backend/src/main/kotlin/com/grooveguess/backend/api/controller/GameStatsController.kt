package com.grooveguess.backend.api.controller

import com.grooveguess.backend.domain.dto.GameStatsDto
import com.grooveguess.backend.domain.dto.RecentGameDto
import com.grooveguess.backend.domain.dto.PaginatedStatsResponseDto
import com.grooveguess.backend.service.GameStatsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory

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
}