package com.grooveguess.backend.domain.repository

import com.grooveguess.backend.domain.model.Track
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface TrackRepository : JpaRepository<Track, Long> {
    fun findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
        title: String,
        artist: String,
        pageable: Pageable
    ): Page<Track>
}