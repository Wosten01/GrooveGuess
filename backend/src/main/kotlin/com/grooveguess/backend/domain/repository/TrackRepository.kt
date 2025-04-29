package com.grooveguess.backend.domain.repository

import com.grooveguess.backend.domain.model.Track
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TrackRepository : JpaRepository<Track, Long> {
    fun findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
        title: String,
        artist: String,
        pageable: Pageable
    ): Page<Track>

    @Query("SELECT t FROM Track t JOIN t.quizzes q WHERE q.id = :quizId")
    fun findByQuizId(quizId: Long): List<Track>
}