package com.grooveguess.backend.domain.repository

import com.grooveguess.backend.domain.model.Track
import org.springframework.data.jpa.repository.JpaRepository

interface TrackRepository : JpaRepository<Track, Long>