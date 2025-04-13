package com.grooveguess.domain.repository

import com.grooveguess.domain.model.Track
import org.springframework.data.jpa.repository.JpaRepository

interface TrackRepository : JpaRepository<Track, Long>