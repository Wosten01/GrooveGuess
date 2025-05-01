package com.grooveguess.backend.domain.repository

import com.grooveguess.backend.domain.model.Track
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TrackRepository : JpaRepository<Track, Long> {
    fun findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
        title: String,
        artist: String,
        pageable: Pageable
    ): Page<Track>

    @Query(value = """
        SELECT t.* FROM tracks t
        JOIN quiz_tracks qt ON t.id = qt.track_id
        WHERE qt.quiz_id = :quizId
        ORDER BY RANDOM()
        LIMIT :limit
    """, nativeQuery = true)
    fun findRandomTracksByQuizIdWithLimit(@Param("quizId") quizId: Long, @Param("limit") limit: Long): List<Track>
}