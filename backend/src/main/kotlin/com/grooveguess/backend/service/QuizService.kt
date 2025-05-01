package com.grooveguess.backend.service

import com.grooveguess.backend.domain.model.Quiz
import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.repository.TrackRepository
import com.grooveguess.backend.domain.repository.QuizRepository
import com.grooveguess.backend.domain.repository.UserRepository
import com.grooveguess.backend.domain.dto.AnswerResponse
import com.grooveguess.backend.domain.dto.QuizRequest
import com.grooveguess.backend.domain.dto.QuizDto
import com.grooveguess.backend.domain.dto.UserDto
import com.grooveguess.backend.domain.dto.TrackDto
import org.springframework.stereotype.Service
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import com.grooveguess.backend.domain.enum.AnswerStatus
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import com.grooveguess.backend.domain.enum.Role

@Service
class QuizService(
    private val quizRepository: QuizRepository,
    private val trackRepository: TrackRepository,
    private val userService: UserService,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(QuizService::class.java)

    fun create(quizRequest: QuizRequest, creatorId: Long): Quiz {
        logger.debug("Attempting to create quiz with title='${quizRequest.title}' by userId=$creatorId")
        if (!userService.isAdmin(creatorId)) {
            logger.warn("User $creatorId is not admin, cannot create quiz")
            throw IllegalAccessException("Only admins can create quizzes")
        }
        val creator = userRepository.findById(creatorId)
            .orElseThrow { IllegalArgumentException("Creator user not found") }
        val tracks = if (quizRequest.trackIds.isNotEmpty())
            trackRepository.findAllById(quizRequest.trackIds)
        else
            emptyList()
        val quizToSave = quizRequest.toEntity(
            tracks = tracks,
            creator = creator,
            createdAt = LocalDateTime.now()
        )
        validateQuiz(quizToSave)
        val savedQuiz = quizRepository.save(quizToSave)
        logger.debug("Quiz created successfully with id=${savedQuiz.id}")
        return savedQuiz
    }

    fun find(id: Long): Quiz {
        logger.debug("Fetching quiz with id=$id")
        return quizRepository.findById(id)
            .orElseThrow { RuntimeException("Quiz not found") }
    }

    fun findAll(page: Int, size: Int): Page<QuizDto> {
        val pageable = PageRequest.of(page, size)
        val quizPage = quizRepository.findAll(pageable)

        val quizDtos = quizPage.content.map { quiz ->
            val creator = quiz.creator
            val tracks = quiz.tracks
            QuizDto(
                id = quiz.id,
                title = quiz.title,
                description = quiz.description,
                roundCount = quiz.roundCount,
                creator = UserDto(
                    id = creator.id,
                    username = creator.username,
                    email = creator.email,
                    role=  creator.role,
                ),
                tracks = tracks.map { track ->
                    TrackDto(
                        id = track.id,
                        title = track.title,
                        artist = track.artist,
                        url = track.url,
                    )
                }
            )
        }
        return PageImpl(quizDtos, pageable, quizPage.totalElements)
    }

    fun update(id: Long, updatedQuiz: Quiz, userId: Long): Quiz? {
        logger.debug("Attempting to update quiz id=$id by userId=$userId")
        if (!userService.isAdmin(userId)) {
            logger.warn("User $userId is not admin, cannot update quiz")
            throw IllegalAccessException("Only admins can update quizzes")
        }
        validateQuiz(updatedQuiz)
        return quizRepository.findById(id).map { existingQuiz ->
            val newQuiz = existingQuiz.copy(
                title = updatedQuiz.title,
                description = updatedQuiz.description,
                roundCount = updatedQuiz.roundCount,
                tracks = updatedQuiz.tracks
            )
            val savedQuiz = quizRepository.save(newQuiz)
            logger.debug("Quiz id=$id updated successfully")
            savedQuiz
        }.orElseGet {
            logger.warn("Quiz id=$id not found for update")
            null
        }
    }

    fun getTracksByIds(trackIds: List<Long>): List<Track> {
        if (trackIds.isEmpty()) return emptyList()
        val tracks = trackRepository.findAllById(trackIds)
        if (tracks.size != trackIds.size) {
            logger.warn("Not all tracks found for ids: $trackIds")
            throw IllegalArgumentException("Some tracks not found")
        }
        return tracks
    }

    fun delete(id: Long, userId: Long) {
        logger.debug("Attempting to delete quiz id=$id by userId=$userId")
        if (!userService.isAdmin(userId)) {
            logger.warn("User $userId is not admin, cannot delete quiz")
            throw IllegalAccessException("Only admins can delete quizzes")
        }
        quizRepository.deleteById(id)
        logger.debug("Quiz id=$id deleted successfully")
    }

    fun addTrackToQuiz(quizId: Long, trackId: Long, userId: Long): Quiz {
        return addTracksToQuiz(quizId, listOf(trackId), userId)
    }

    fun addTracksToQuiz(quizId: Long, trackIds: List<Long>, userId: Long): Quiz {
        logger.debug("Attempting to add tracks $trackIds to quiz id=$quizId by userId=$userId")
        if (!userService.isAdmin(userId)) {
            logger.warn("User $userId is not admin, cannot add tracks to quiz")
            throw IllegalAccessException("Only admins can add tracks to quizzes")
        }
        val quiz = find(quizId)
        val tracks = trackRepository.findAllById(trackIds)
        if (tracks.size != trackIds.size) {
            logger.warn("Some tracks not found for ids $trackIds")
            throw RuntimeException("Some tracks not found")
        }
        val updatedTracks = quiz.tracks + tracks
        val updatedQuiz = quiz.copy(tracks = updatedTracks)
        validateQuiz(updatedQuiz)
        val savedQuiz = quizRepository.save(updatedQuiz)
        logger.debug("Tracks $trackIds added to quiz id=$quizId successfully")
        return savedQuiz
    }

    private fun validateQuiz(quiz: Quiz) {
        if (quiz.roundCount <= 1) {
            logger.warn("Quiz validation failed: not enough rounds")
            throw IllegalArgumentException("Quiz must have at least 2 rounds")
        }
        // if (quiz.tracks.size < quiz.roundCount) {
        //     logger.warn("Quiz validation failed: not enough tracks for rounds")
        //     throw IllegalArgumentException("Quiz must have at least ${quiz.roundCount} tracks")
        // }
    }
}