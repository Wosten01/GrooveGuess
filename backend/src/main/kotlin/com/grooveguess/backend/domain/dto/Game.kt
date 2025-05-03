package com.grooveguess.backend.domain.dto

import java.util.UUID
import java.io.Serializable

data class GameSessionDto(
    val sessionId: String,
    val totalRounds: Int,
    val currentRoundNumber: Int,
    val score: Int,
    val completed: Boolean = false,
    val currentRound: RoundDto? = null
)

data class TrackOptionDto(
    val id: Long,
    val title: String,
    val artist: String,
): Serializable

data class AnswerDto(
    val roundNumber: Int,
    val optionId: Long
)

data class AnswerResultDto(
    val correct: Boolean,
    val points: Int,
    val isLastRound: Boolean,
    val finalScore: Int
)

data class RoundDto(
    val currentRound: Int,
    val url: String,
    val options: List<TrackOptionDto>
)

data class UserAnswerDto(
    val roundNumber: Int,
    val selectedOptionId: Long,
    val isCorrect: Boolean
)

data class GameResultsDto(
    val quizId: Long,
    val totalRounds: Int,
    val score: Int,
    val tracks: List<TrackResultDto>,
    var userAnswers: List<UserAnswerDto>,
)

data class TrackResultDto(
    val roundNumber: Int,
    val trackId: Long,
    val title: String,
    val artist: String,
    val url: String,
    val wasGuessed: Boolean,
    val options: List<TrackOptionDto>,
    val userAnswer: UserAnswerDto? = null,
)