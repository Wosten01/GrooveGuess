package com.grooveguess.backend.service

import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.repository.TrackRepository
import com.grooveguess.backend.service.UserService
import org.springframework.stereotype.Service

@Service
class TrackService(private val trackRepository: TrackRepository, private val userService: UserService) {

    fun create(track: Track, creatorId: Long): Track {
        if (!userService.isAdmin(creatorId)) {
            throw IllegalAccessException("Only admins can create tracks")
        }
        return trackRepository.save(track)
    }

    fun find(id: Long): Track = trackRepository.findById(id)
        .orElseThrow { RuntimeException("Track not found") }

    fun findAll(): List<Track> = trackRepository.findAll()

    fun update(id: Long, updatedTrack: Track, userId: Long): Track? {
        if (!userService.isAdmin(userId)) {
            throw IllegalAccessException("Only admins can update tracks")
        }
        return trackRepository.findById(id).map {
            val newTrack = it.copy(
                title = updatedTrack.title,
                artist = updatedTrack.artist,
                url = updatedTrack.url
            )
            trackRepository.save(newTrack)
        }.orElse(null)
    }

    fun delete(id: Long, userId: Long) {
        if (!userService.isAdmin(userId)) {
            throw IllegalAccessException("Only admins can delete tracks")
        }
        trackRepository.deleteById(id)
    }
}
