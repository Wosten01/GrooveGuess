package com.grooveguess.backend.domain.dto

import AnswerStatus

data class AnswerResponse(
    val status: AnswerStatus,
    val score: Int,
    val correctTrackId: Long
)