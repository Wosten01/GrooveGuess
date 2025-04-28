package com.grooveguess.backend.api.controller

import com.grooveguess.backend.domain.dto.GameRound
import com.grooveguess.backend.domain.model.Quiz
import com.grooveguess.backend.service.QuizService
import com.grooveguess.backend.domain.dto.AnswerResponse
import com.grooveguess.backend.domain.dto.QuizResponse
import com.grooveguess.backend.domain.dto.QuizRequest
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest

@RestController
@RequestMapping("/api/quizzes")
class QuizController(private val quizService: QuizService) {

    private val logger = LoggerFactory.getLogger(QuizController::class.java)

    @GetMapping
    fun QuizResponse(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): Page<QuizResponse> {
        logger.debug("Fetching all quizzes with page=$page, size=$size")
        val quizPage = quizService.findAll(page, size)
        return quizPage.map { QuizResponse.fromEntity(it) }
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): QuizResponse {
        logger.debug("Fetching quiz by id=$id")
        return QuizResponse.fromEntity(quizService.find(id))
    }

    @PostMapping
    fun create(@RequestBody request: QuizRequest, @RequestParam creatorId: Long): QuizResponse {
        logger.debug("Creating quiz with title='${request.title}' by creatorId=$creatorId")
        val created = quizService.create(request, creatorId)
        return QuizResponse.fromEntity(created)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody quiz: QuizRequest,
        @RequestParam userId: Long
    ): QuizResponse? {
        logger.debug("Updating quiz id=$id by userId=$userId")
        val existingQuiz = quizService.find(id)
        val newTracks = if (quiz.trackIds.isNotEmpty())
            quizService.getTracksByIds(quiz.trackIds)
        else
            emptyList()
        val updated = quizService.update(
            id,
            quiz.toEntity(
                tracks = newTracks,
                creator = existingQuiz.creator,
                createdAt = existingQuiz.createdAt
            ),
            userId
        )
        return updated?.let { QuizResponse.fromEntity(it) }
    }


    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, @RequestParam userId: Long) {
        logger.debug("Deleting quiz id=$id by userId=$userId")
        quizService.delete(id, userId)
    }

    @PostMapping("/{id}/tracks")
    fun addTrack(
        @PathVariable id: Long,
        @RequestParam trackId: Long,
        @RequestParam userId: Long
    ): QuizResponse {
        logger.debug("Adding track $trackId to quiz $id by userId=$userId")
        val updated = quizService.addTrackToQuiz(id, trackId, userId)
        return QuizResponse.fromEntity(updated)
    }

    @PostMapping("/{id}/tracks/bulk")
    fun addTracks(
        @PathVariable id: Long,
        @RequestBody trackIds: List<Long>,
        @RequestParam userId: Long
    ): QuizResponse {
        logger.debug("Adding tracks $trackIds to quiz $id by userId=$userId")
        val updated = quizService.addTracksToQuiz(id, trackIds, userId)
        return QuizResponse.fromEntity(updated)
    }
}