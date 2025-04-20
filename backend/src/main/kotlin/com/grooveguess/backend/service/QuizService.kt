package com.grooveguess.service

import com.grooveguess.domain.model.Quiz
import com.grooveguess.domain.repository.QuizRepository
import org.springframework.stereotype.Service

@Service
class QuizService(
    private val quizRepository: QuizRepository,
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

    fun submitAnswer(quizId: Long, userId: Long, trackId: Long, isCorrect: Boolean): Int {
        val quiz = find(quizId)
        val user = userService.find(userId)
        if (isCorrect) {
            val updatedUser = user.copy(score = user.score + 10)
            userService.update(userId, updatedUser)
            return updatedUser.score
        }
        return user.score
    }

    private fun validateQuiz(quiz: Quiz) {
        if (quiz.tracks.size < quiz.roundCount) {
            throw IllegalArgumentException("Quiz must have at least ${quiz.roundCount} tracks")
        }
    }
}