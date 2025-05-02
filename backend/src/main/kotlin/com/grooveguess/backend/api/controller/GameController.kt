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
    fun startGame(@PathVariable quizId: Long, @RequestParam userId: Long): ResponseEntity<GameSessionDto> {
        logger.info("Starting game for quiz $quizId and user $userId")
        val session = gameService.startGame(quizId, userId)
        return ResponseEntity.ok(session)
    }

    @GetMapping("/player/{userId}/session/{sessionId}/next-round")
    fun getNextRound(@PathVariable sessionId: String, @PathVariable userId: Long): ResponseEntity<RoundDto> {
        logger.info("Getting next round for session $sessionId and user $userId")
        val round = gameService.getNextRound(sessionId, userId)
        return ResponseEntity.ok(round)
    }

    @PostMapping("/player/{userId}/session/{sessionId}/answer")
    fun submitAnswer(
        @PathVariable sessionId: String,
        @PathVariable userId: Long,
        @RequestBody answer: AnswerDto
    ): ResponseEntity<AnswerResultDto> {
        logger.info("Submitting answer for session $sessionId, user $userId, round ${answer.roundNumber}")
        val result = gameService.submitAnswer(sessionId, answer, userId)
        return ResponseEntity.ok(result)
    }
    
    @GetMapping("/player/{userId}/session/{sessionId}/results")
    fun getGameResults(@PathVariable sessionId: String, @PathVariable userId: Long): ResponseEntity<GameResultsDto> {
        logger.info("Getting game results for session $sessionId and user $userId")
        val results = gameService.getGameResults(sessionId, userId)
        return ResponseEntity.ok(results)
    }
}