package com.grooveguess.backend.api.controller

import com.grooveguess.backend.domain.dto.GameRound
import com.grooveguess.backend.domain.model.Quiz
import com.grooveguess.backend.service.QuizService
import com.grooveguess.backend.domain.dto.AnswerResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/quizzes")
class QuizController(private val quizService: QuizService) {

    @GetMapping
    fun getAll(): List<Quiz> = quizService.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Quiz = quizService.find(id)

    @PostMapping
    fun create(@RequestBody quiz: Quiz, @RequestParam creatorId: Long): Quiz =
        quizService.create(quiz, creatorId)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody quiz: Quiz, @RequestParam userId: Long): Quiz? =
        quizService.update(id, quiz, userId)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, @RequestParam userId: Long) {
        quizService.delete(id, userId)
    }

    @PostMapping("/{id}/tracks")
    fun addTrack(@PathVariable id: Long, @RequestParam trackId: Long, @RequestParam userId: Long): Quiz =
        quizService.addTrackToQuiz(id, trackId, userId)

    @PostMapping("/{id}/tracks/bulk")
    fun addTracks(@PathVariable id: Long, @RequestBody trackIds: List<Long>, @RequestParam userId: Long): Quiz =
        quizService.addTracksToQuiz(id, trackIds, userId)

    @GetMapping("/{id}/game-rounds")
    fun getGameRounds(@PathVariable id: Long, @RequestParam tracksPerRound: Int): List<GameRound> =
        quizService.getGameRounds(id, tracksPerRound)

    @PostMapping("/{id}/answer")
    fun submitAnswer(
        @PathVariable id: Long,
        @RequestParam userId: Long,
        @RequestParam selectedTrackId: Long,
        @RequestParam correctTrackId: Long
    ): AnswerResponse = quizService.submitAnswer(id, userId, selectedTrackId, correctTrackId)
}