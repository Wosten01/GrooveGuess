package com.grooveguess.backend.controller

import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import com.grooveguess.backend.domain.dto.AnswerResultDto
import com.grooveguess.backend.domain.dto.AnswerDto
import com.grooveguess.backend.domain.dto.QuizRoundDto
import com.grooveguess.backend.domain.dto.QuizGameSessionDto
import com.grooveguess.backend.service.QuizGameService

@RestController
@RequestMapping("/api/quiz-game")
class QuizGameController(
    private val quizGameService: QuizGameService
) {

    @PostMapping("/{quizId}/start")
    fun startGame(@PathVariable quizId: Long, @RequestParam userId: Long): ResponseEntity<QuizGameSessionDto> {
        val session = quizGameService.startGame(quizId, userId)
        return ResponseEntity.ok(session)
    }

    @GetMapping("/{quizId}/next-round")
    fun getNextRound(
        @PathVariable quizId: Long,
        @RequestParam userId: Long
    ): ResponseEntity<QuizRoundDto> {
        val round = quizGameService.getNextRound(quizId, userId)
        return if (round != null) ResponseEntity.ok(round)
        else ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    @PostMapping("/{quizId}/answer")
    fun submitAnswer(
        @PathVariable quizId: Long,
        @RequestParam userId: Long,
        @RequestBody answer: AnswerDto
    ): ResponseEntity<AnswerResultDto> {
        val result = quizGameService.submitAnswer(quizId, userId, answer)
        return ResponseEntity.ok(result)
    }
}