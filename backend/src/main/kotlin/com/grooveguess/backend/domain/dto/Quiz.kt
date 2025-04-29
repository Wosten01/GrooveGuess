package com.grooveguess.backend.domain.dto

import com.grooveguess.backend.domain.model.Quiz
import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.model.Track
import java.time.LocalDateTime
import com.grooveguess.backend.domain.enum.Role
import com.grooveguess.backend.domain.dto.TrackDto


data class QuizRequest(
    val title: String,
    val description: String?,
    val roundCount: Int,
    val trackIds: List<Long> = emptyList()
) {
    fun toEntity(tracks: List<Track> = emptyList(), creator: User, createdAt: LocalDateTime? = null): Quiz {
        return Quiz(
            id = 0,
            title = title,
            description = description,
            roundCount = roundCount,
            tracks = tracks,
            creator = creator , 
            createdAt = createdAt ?: LocalDateTime.now()
        )
    }
}

data class QuizResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val roundCount: Int,
    val trackIds: List<Long>,
    val creatorId: Long,
    val createdAt: LocalDateTime
) {
    companion object {
        fun fromEntity(quiz: Quiz): QuizResponse = QuizResponse(
            id = quiz.id,
            title = quiz.title,
            description = quiz.description,
            roundCount = quiz.roundCount,
            trackIds = quiz.tracks.map { it.id },
            creatorId = quiz.creator.id,
            createdAt = quiz.createdAt
        )
    }
}


data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    val role: Role,
)

data class QuizDto(
    val id: Long,
    val title: String,
    val description: String?,
    val roundCount: Int,
    val creator: UserDto,
    val tracks: List<TrackDto>
)
