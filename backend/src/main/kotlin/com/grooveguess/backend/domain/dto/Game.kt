package com.grooveguess.backend.domain.dto

import java.util.UUID
import java.io.Serializable

data class GameSessionDto(
    val sessionId: String,
    val totalRounds: Int,
    val score: Int,
    val currentRound: Int
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
    val isLastRound: Boolean
)