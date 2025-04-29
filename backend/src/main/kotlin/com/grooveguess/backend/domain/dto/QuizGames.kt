package com.grooveguess.backend.domain.dto

data class QuizGameSessionDto(
    val sessionId: String,
    val totalRounds: Int,
    val currentScore: Int
)

data class QuizRoundDto(
    val roundNumber: Int,
    val totalRounds: Int,
    val audioUrl: String,
    val options: List<OptionDto>
)

data class OptionDto(
    val id: Long,
    val title: String
)

data class AnswerDto(
    val roundNumber: Int,
    val optionId: Long
)

data class AnswerResultDto(
    val correct: Boolean,
    val points: Int,
    val isLastRound: Boolean
)