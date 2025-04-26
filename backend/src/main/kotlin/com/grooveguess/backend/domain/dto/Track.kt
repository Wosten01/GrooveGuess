package com.grooveguess.backend.domain.dto

data class TrackRequest(
    val title: String,
    val artist: String,
    val url: String
)