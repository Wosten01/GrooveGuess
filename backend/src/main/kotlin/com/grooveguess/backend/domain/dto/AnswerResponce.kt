package com.grooveguess.backend.domain.dto

import com.grooveguess.backend.domain.enum.AnswerStatus


data class AnswerResponse(
    val status: AnswerStatus,
    val score: Int,
    val correctTrackId: Long
)