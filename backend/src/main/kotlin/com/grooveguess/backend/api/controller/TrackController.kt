package com.grooveguess.backend.api.controller

import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.dto.TrackRequest
import com.grooveguess.backend.domain.dto.TrackDto
import com.grooveguess.backend.exception.ResourceNotFoundException
import com.grooveguess.backend.service.TrackService
import com.grooveguess.backend.service.AudioVerificationResult
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page

@RestController
@RequestMapping("/api/tracks")
class TrackController(
    private val trackService: TrackService
) {
    private val logger = LoggerFactory.getLogger(TrackController::class.java)

    @PostMapping
    fun createTrack(
        @RequestBody request: TrackRequest,
        @RequestParam creatorId: Long
    ): ResponseEntity<Track> {
        logger.debug("POST /api/tracks - Attempting to create track: title=${request.title}, artist=${request.artist}, url=${request.url} by user $creatorId")
        val track = Track(
            title = request.title,
            artist = request.artist,
            url = request.url,
            quizzes = emptyList() // Explicitly set quizzes to empty list
        )
        val created = trackService.create(track, creatorId)
        logger.debug("Track created successfully: id=${created.id}, title=${created.title}, artist=${created.artist}")
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @GetMapping("/{id}")
    fun getTrackById(@PathVariable id: Long): ResponseEntity<Track> {
        logger.debug("GET /api/tracks/$id - Fetching track by id")
        val track = trackService.findById(id)
        logger.debug("Track found: id=${track.id}, title=${track.title}, artist=${track.artist}")
        return ResponseEntity.ok(track)
    }

    @GetMapping
    fun getAllTracks(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "") search: String,
    ): Page<TrackDto> {
        logger.debug("GET /api/tracks - Fetching all tracks, page=$page, size=$size, search=$search")
        return trackService.findAll(page, size, search)
    }
    
    @PutMapping("/{id}")
    fun updateTrack(
        @PathVariable id: Long,
        @RequestBody request: TrackRequest,
        @RequestParam userId: Long
    ): ResponseEntity<Track> {
        logger.debug("PUT /api/tracks/$id - Attempting to update track by user $userId with data: title=${request.title}, artist=${request.artist}, url=${request.url}")
        val updatedTrack = Track(
            title = request.title,
            artist = request.artist,
            url = request.url,
            quizzes = emptyList() // Explicitly set quizzes to empty list
        )
        val updated = trackService.update(id, updatedTrack, userId)
            ?: throw ResourceNotFoundException("Track not found with id: $id")
        logger.debug("Track updated successfully: id=${updated.id}, title=${updated.title}, artist=${updated.artist}")
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun deleteTrack(
        @PathVariable id: Long,
        @RequestParam userId: Long
    ): ResponseEntity<Void> {
        logger.debug("DELETE /api/tracks/$id - Attempting to delete track by user $userId")
        trackService.delete(id, userId)
        logger.debug("Track $id deleted successfully")
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/validate-audio")
    fun validateAudioUrl(@RequestParam url: String): ResponseEntity<AudioVerificationResult> {
        logger.debug("GET /api/tracks/validate-audio - Validating audio URL: $url")
        val result = trackService.verifyAudioUrl(url)
        return if (result.isValid) {
            logger.debug("Audio URL is valid: $url, mimeType: ${result.mimeType}")
            ResponseEntity.ok(result)
        } else {
            logger.debug("Audio URL is invalid: $url, error: ${result.error}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result)
        }
    }
}