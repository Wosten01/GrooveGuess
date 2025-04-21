package com.grooveguess.backend.domain.dto

import com.grooveguess.backend.domain.model.Track

data class GameRound(
    val roundNumber: Int,
    val tracks: List<Track>, 
    val correctTrackId: Long
)