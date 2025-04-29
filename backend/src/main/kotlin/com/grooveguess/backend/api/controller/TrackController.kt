
package com.grooveguess.backend.api.controller

import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.dto.TrackRequest
import com.grooveguess.backend.domain.dto.TrackDto
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
        logger.debug("POST /api/tracks - Attempting to create track: $request by user $creatorId")
        return try {
            val track = Track(
                title = request.title,
                artist = request.artist,
                url = request.url
            )
            val created = trackService.create(track, creatorId)
            logger.debug("Track created successfully: $created")
            ResponseEntity.status(HttpStatus.CREATED).body(created)
        } catch (e: IllegalAccessException) {
            logger.debug("User $creatorId is not admin, cannot create track")
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } catch (e: Exception) {
            logger.debug("Failed to create track: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @GetMapping("/{id}")
    fun getTrackById(@PathVariable id: Long): ResponseEntity<Track> {
        logger.debug("GET /api/tracks/$id - Fetching track by id")
        return try {
            val track = trackService.findById(id)
            logger.debug("Track found: $track")
            ResponseEntity.ok(track)
        } catch (e: RuntimeException) {
            logger.debug("Track with id $id not found")
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping
    fun getAllTracks(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "") search: String,
    ): Page<TrackDto> {
        return trackService.findAll(page, size, search)
    }
    
    @PutMapping("/{id}")
    fun updateTrack(
        @PathVariable id: Long,
        @RequestBody request: TrackRequest,
        @RequestParam userId: Long
    ): ResponseEntity<Track> {
        logger.debug("PUT /api/tracks/$id - Attempting to update track by user $userId with data: $request")
        return try {
            val updatedTrack =Track(
                title = request.title,
                artist = request.artist,
                url = request.url
            )
            val updated = trackService.update(id, updatedTrack, userId)
            if (updated != null) {
                logger.debug("Track updated successfully: $updated")
                ResponseEntity.ok(updated)
            } else {
                logger.debug("Track with id $id not found for update")
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalAccessException) {
            logger.debug("User $userId is not admin, cannot update track")
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } catch (e: RuntimeException) {
            logger.debug("Failed to update track: ${e.message}")
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteTrack(
        @PathVariable id: Long,
        @RequestParam userId: Long
    ): ResponseEntity<Void> {
        logger.debug("DELETE /api/tracks/$id - Attempting to delete track by user $userId")
        return try {
            trackService.delete(id, userId)
            logger.debug("Track $id deleted successfully")
            ResponseEntity.noContent().build()
        } catch (e: IllegalAccessException) {
            logger.debug("User $userId is not admin, cannot delete track")
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
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
