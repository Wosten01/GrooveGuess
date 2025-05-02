package com.grooveguess.backend.api.controller

import com.grooveguess.backend.domain.dto.*
import com.grooveguess.backend.service.GameService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/quiz-game")
class GameController(private val gameService: GameService) {
    private val logger = LoggerFactory.getLogger(GameController::class.java)

    @PostMapping("/{quizId}/start")
    fun startGame(@PathVariable quizId: Long, @RequestParam userId: Long): ResponseEntity<Any> {
        return try {
            val session = gameService.startGame(quizId, userId)
            ResponseEntity.ok(session)
        } catch (e: IllegalArgumentException) {
            logger.warn("Failed to start game: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to (e.message ?: "Invalid request")))
        } catch (e: Exception) {
            logger.error("Unexpected error starting game: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Internal server error"))
        }
    }

    @GetMapping("/player/{userId}/session/{sessionId}/next-round")
    fun getNextRound(@PathVariable sessionId: String, @PathVariable userId: Long): ResponseEntity<Any> {
        return try {
            val round = gameService.getNextRound(sessionId, userId)
            ResponseEntity.ok(round)
        } catch (e: IllegalAccessException) {
            logger.warn("User $userId has no access to session $sessionId")
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("message" to "You don't have access to this session"))
        } catch (e: IllegalStateException) {
            when (e.message) {
                "Session not found" -> {
                    logger.warn("Session not found: $sessionId")
                    ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(mapOf("message" to "Session not found"))
                }
                "No more rounds available" -> {
                    logger.info("No more rounds available for session $sessionId")
                    ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(mapOf("message" to "No more rounds available"))
                }
                "Game is already completed" -> {
                    logger.info("Game is already completed for session $sessionId")
                    ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(mapOf("message" to "Game is already completed"))
                }
                else -> {
                    logger.warn("State error in getNextRound: ${e.message}")
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(mapOf("message" to (e.message ?: "Invalid state")))
                }
            }
        } catch (e: Exception) {
            logger.error("Unexpected error in getNextRound: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Internal server error"))
        }
    }

    @PostMapping("/player/{userId}/session/{sessionId}/answer")
    fun submitAnswer(
        @PathVariable sessionId: String,
        @PathVariable userId: Long,
        @RequestBody answer: AnswerDto
    ): ResponseEntity<Any> {
        return try {
            val result = gameService.submitAnswer(sessionId, answer, userId)
            ResponseEntity.ok(result)
        } catch (e: IllegalAccessException) {
            logger.warn("User $userId has no access to session $sessionId")
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("message" to "You don't have access to this session"))
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid argument in submitAnswer: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to (e.message ?: "Invalid request")))
        } catch (e: IllegalStateException) {
            when (e.message) {
                "Game is already completed" -> {
                    logger.info("Game is already completed for session $sessionId")
                    ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(mapOf("message" to "Game is already completed"))
                }
                else -> {
                    logger.warn("State error in submitAnswer: ${e.message}")
                    ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(mapOf("message" to (e.message ?: "Invalid state")))
                }
            }
        } catch (e: Exception) {
            logger.error("Unexpected error in submitAnswer: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Internal server error"))
        }
    }
    
    @GetMapping("/player/{userId}/session/{sessionId}/results")
    fun getGameResults(@PathVariable sessionId: String, @PathVariable userId: Long): ResponseEntity<Any> {
        return try {
            val results = gameService.getGameResults(sessionId, userId)
            ResponseEntity.ok(results)
        } catch (e: IllegalAccessException) {
            logger.warn("User $userId has no access to session $sessionId")
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("message" to "You don't have access to this session"))
        } catch (e: IllegalStateException) {
            when (e.message) {
                "Session not found" -> {
                    logger.warn("Session not found: $sessionId")
                    ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(mapOf("message" to "Session not found"))
                }
                else -> {
                    logger.warn("State error in getGameResults: ${e.message}")
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(mapOf("message" to (e.message ?: "Invalid state")))
                }
            }
        } catch (e: Exception) {
            logger.error("Unexpected error in getGameResults: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Internal server error"))
        }
    }
}