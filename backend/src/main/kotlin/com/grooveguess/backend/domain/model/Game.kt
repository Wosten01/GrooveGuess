
package com.grooveguess.backend.domain.model

import com.grooveguess.backend.domain.dto.TrackOptionDto
import com.grooveguess.backend.domain.dto.UserAnswerDto
import java.io.Serializable

import java.time.LocalDateTime
import java.time.ZoneOffset

data class GameSession(
    val sessionId: String,
    val quizId: Long,
    val userId: Long,
    val rounds: List<Round>,
    var currentRound: Int,
    var score: Int,
    var completed: Boolean = false,
    var wonRounds: List<Int> = emptyList(),
    var userAnswers: List<UserAnswerDto> = emptyList(),
    var timestamp: Long = -1,
) : Serializable

data class Round(
    val roundNumber: Int,
    val url: String,
    val options: List<TrackOptionDto>,
    val correctTrackId: Long,
    var checked: Boolean
) : Serializable