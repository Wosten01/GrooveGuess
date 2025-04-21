package com.grooveguess.backend.service

import com.grooveguess.backend.domain.dto.GameRound
import com.grooveguess.backend.domain.model.Quiz
import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.repository.TrackRepository
import com.grooveguess.backend.domain.repository.QuizRepository
import com.grooveguess.backend.domain.dto.AnswerResponse
import com.grooveguess.backend.service.UserService
import org.springframework.stereotype.Service
import com.grooveguess.backend.domain.enum.AnswerStatus

@Service
class QuizService(
    private val quizRepository: QuizRepository,
    private val trackRepository: TrackRepository,
    private val userService: UserService
) {

    fun create(quiz: Quiz, creatorId: Long): Quiz {
        if (!userService.isAdmin(creatorId)) {
            throw IllegalAccessException("Only admins can create quizzes")
        }
        validateQuiz(quiz)
        return quizRepository.save(quiz)
    }

    fun find(id: Long): Quiz = quizRepository.findById(id)
        .orElseThrow { RuntimeException("Quiz not found") }

    fun findAll(): List<Quiz> = quizRepository.findAll()

    fun update(id: Long, updatedQuiz: Quiz, userId: Long): Quiz? {
        if (!userService.isAdmin(userId)) {
            throw IllegalAccessException("Only admins can update quizzes")
        }
        validateQuiz(updatedQuiz)
        return quizRepository.findById(id).map {
            val newQuiz = it.copy(
                title = updatedQuiz.title,
                description = updatedQuiz.description,
                roundCount = updatedQuiz.roundCount,
                tracks = updatedQuiz.tracks
            )
            quizRepository.save(newQuiz)
        }.orElse(null)
    }

    fun delete(id: Long, userId: Long) {
        if (!userService.isAdmin(userId)) {
            throw IllegalAccessException("Only admins can delete quizzes")
        }
        quizRepository.deleteById(id)
    }

    fun addTrackToQuiz(quizId: Long, trackId: Long, userId: Long): Quiz {
       return addTracksToQuiz(quizId, listOf(trackId), userId)
    }

    fun addTracksToQuiz(quizId: Long, trackIds: List<Long>, userId: Long): Quiz {
        if (!userService.isAdmin(userId)) {
            throw IllegalAccessException("Only admins can add tracks to quizzes")
        }
        val quiz = find(quizId)
        val tracks = trackRepository.findAllById(trackIds)
        if (tracks.size != trackIds.size) {
            throw RuntimeException("Some tracks not found")
        }
        val updatedTracks = quiz.tracks + tracks
        val updatedQuiz = quiz.copy(tracks = updatedTracks)
        validateQuiz(updatedQuiz)
        return quizRepository.save(updatedQuiz)
    }

    fun getGameRounds(quizId: Long, tracksPerRound: Int): List<GameRound> {
        val quiz = find(quizId)
        if (quiz.tracks.isEmpty()) {
            throw IllegalStateException("Quiz has no tracks")
        }
        if (tracksPerRound < 2) {
            throw IllegalArgumentException("Tracks per round must be at least 2")
        }

        // Перемешиваем треки
        val shuffledTracks = quiz.tracks.shuffled()

        // Разделяем на раунды
        val rounds = mutableListOf<GameRound>()
        val totalRounds = quiz.roundCount
        val availableTracks = shuffledTracks.take(totalRounds * tracksPerRound)

        for (round in 0 until totalRounds) {
            val startIndex = round * tracksPerRound
            if (startIndex >= availableTracks.size) break

            // Берем треки для текущего раунда (например, 4 варианта ответа)
            val roundTracks = availableTracks
                .subList(startIndex, minOf(startIndex + tracksPerRound, availableTracks.size))
                .toMutableList()

            // Если треков меньше, чем tracksPerRound, добавляем случайные треки из базы
            // while (roundTracks.size < tracksPerRound && availableTracks.size > roundTracks.size) {
            //     val extraTrack = trackRepository.findAll()
            //         .filter { it !in quiz.tracks }
            //         .shuffled()
            //         .firstOrNull() ?: break
            //     roundTracks.add(extraTrack)
            // }

            if (roundTracks.isEmpty()) break

            // Выбираем правильный трек для угадывания
            val correctTrack = roundTracks.random()

            rounds.add(
                GameRound(
                    roundNumber = round + 1,
                    tracks = roundTracks,
                    correctTrackId = correctTrack.id
                )
            )
        }

        return rounds
    }

    fun submitAnswer(quizId: Long, userId: Long, selectedTrackId: Long, correctTrackId: Long): AnswerResponse {
        val quiz = find(quizId)
        val user = userService.find(userId)
        val isCorrect = selectedTrackId == correctTrackId && quiz.tracks.any { it.id == correctTrackId }

        val updatedUser = if (isCorrect) {
            user.copy(score = user.score + 10)
        } else {
            user
        }

        userService.update(userId, updatedUser)

        return AnswerResponse(
            status = if (isCorrect) AnswerStatus.CORRECT else AnswerStatus.INCORRECT,
            score = updatedUser.score,
            correctTrackId = correctTrackId
        )
    }

    private fun validateQuiz(quiz: Quiz) {
        if (quiz.tracks.size < quiz.roundCount) {
            throw IllegalArgumentException("Quiz must have at least ${quiz.roundCount} tracks")
        }
    }
}