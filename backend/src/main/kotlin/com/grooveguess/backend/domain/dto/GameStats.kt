package com.grooveguess.backend.domain.dto

data class RecentGameDto(
    val sessionId: String,
    val userId: Long,
    val username: String,
    val quizId: Long,
    val score: Int,
    val totalRounds: Int,
    val correctAnswers: Int,
    val timestamp: Long
)

data class GameStatsDto(
    val totalGames: Int,
    val totalScore: Int,
    val averageScore: Int,
    val highestScore: Int,
    val accuracy: Int,
)

data class PaginatedStatsResponseDto(
    val games: List<RecentGameDto>,
    val totalGames: Long,
    val totalPages: Int,
    val currentPage: Int,
    val stats: GameStatsDto? = null
)