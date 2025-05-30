package com.grooveguess.backend.service

import com.grooveguess.backend.domain.dto.TrackDto
import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.repository.TrackRepository
import com.grooveguess.backend.domain.repository.QuizRepository
import org.apache.hc.client5.http.classic.methods.HttpHead
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.HttpStatus
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.net.URI
import jakarta.transaction.Transactional

data class AudioVerificationResult(
    val isValid: Boolean,
    val mimeType: String? = null,
    val error: String? = null,
)

@Service
class TrackService(
    private val trackRepository: TrackRepository,
    private val userService: UserService,
    private val quizRepository: QuizRepository
) {
    private val logger = LoggerFactory.getLogger(TrackService::class.java)

    private val validAudioMimeTypes =
        setOf(
            "audio/mpeg",
            "audio/mp3",
            "audio/wav",
            "audio/ogg",
            "audio/aac",
            "audio/flac",
            "audio/webm",
        )
    
    private val validAudioExtensions =
        setOf(
            ".mp3",
            ".wav",
            ".ogg",
            ".aac",
            ".flac",
            ".webm",
        )

    fun create(
        track: Track,
        userId: Long,
    ): Track {
        logger.debug("Attempting to create track: $track by user $userId")
        if (!userService.isAdmin(userId)) {
            logger.debug("User $userId is not admin, cannot create track")
            throw IllegalAccessException("Only admins can create tracks")
        }
        val saved = trackRepository.save(track)
        logger.debug("Track created: $saved")
        return saved
    }

    fun findById(id: Long): Track =
        trackRepository.findById(id).orElseThrow {
            logger.debug("Track with id $id not found")
            RuntimeException("Track not found")
        }

    fun findAll(
        page: Int,
        size: Int,
        search: String? = null,
        sortBy: String = "id",
        sortDirection: String = "asc",
    ): Page<TrackDto> {
        val direction =
            if (sortDirection.equals("asc", ignoreCase = true)) {
                Sort.Direction.DESC
            } else {
                Sort.Direction.ASC
            }
        val sort = Sort.by(direction, sortBy)
        val pageable = PageRequest.of(page, size, sort)

        val trimmedSearch = search?.trim()
        logger.debug("Searching tracks with term: '$trimmedSearch', sorting by: $sortBy $sortDirection")

        val trackPage: Page<Track> =
            if (!trimmedSearch.isNullOrEmpty()) {
                trackRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
                    trimmedSearch,
                    trimmedSearch,
                    pageable,
                )
            } else {
                logger.debug("Fetching all tracks without search filter")
                trackRepository.findAll(pageable)
            }

        logger.debug("Found ${trackPage.totalElements} tracks: ${trackPage.content.map { it.title }}")
        val trackDtos =
            trackPage.content.map { track ->
                TrackDto(
                    id = track.id,
                    title = track.title,
                    artist = track.artist,
                    url = track.url,
                )
            }
        return PageImpl(trackDtos, pageable, trackPage.totalElements)
    }

    fun update(
        id: Long,
        updatedTrack: Track,
        userId: Long,
    ): Track? {
        logger.debug("Attempting to update track $id by user $userId")
        if (!userService.isAdmin(userId)) {
            logger.debug("User $userId is not admin, cannot update track")
            throw IllegalAccessException("Only admins can update tracks")
        }

        val audioVerificationResult = verifyAudioUrl(updatedTrack.url)
        logger.debug("Audio from URL ${updatedTrack.url} is ${if (audioVerificationResult.isValid) "valid" else "invalid"}.")

        if (audioVerificationResult.error != null || !audioVerificationResult.isValid) {
            logger.debug("Audio verification failed: ${audioVerificationResult.error}")
            throw RuntimeException(audioVerificationResult.error)
        }

        logger.debug("mime type is ${audioVerificationResult.mimeType}.")

        return trackRepository
            .findById(id)
            .map {
                val newTrack =
                    it.copy(
                        title = updatedTrack.title,
                        artist = updatedTrack.artist,
                        url = updatedTrack.url,
                    )
                val saved = trackRepository.save(newTrack)
                logger.debug("Track updated: $saved")
                saved
            }.orElse(null)
    }

    @Transactional
    fun delete(
        id: Long,
        userId: Long,
    ) {
        logger.debug("Attempting to delete track $id by user $userId")
        if (!userService.isAdmin(userId)) {
            logger.debug("User $userId is not admin, cannot delete track")
            throw IllegalAccessException("Only admins can delete tracks")
        }
        
        val track = trackRepository.findById(id)
            .orElseThrow { NoSuchElementException("Track with id $id not found") }
        
        for (quiz in track.quizzes) {
            val updatedTracks = quiz.tracks.filter { it.id != id }
            val updatedQuiz = quiz.copy(tracks = updatedTracks)
            quizRepository.save(updatedQuiz)
        }

        trackRepository.deleteById(id)
        logger.debug("Track $id deleted")
    }

    fun verifyAudioUrl(url: String): AudioVerificationResult {
        logger.debug("Verifying audio URL: $url")
        return try {
            val parsedUrl =
                try {
                    URI(url)
                } catch (e: Exception) {
                    logger.error("Invalid URL format: $url", e)
                    return AudioVerificationResult(
                        isValid = false,
                        error = "Invalid URL format: ${e.message}",
                    )
                }

            // Special handling for Dropbox URLs
            if (url.contains("dropbox.com")) {
                logger.debug("Detected Dropbox URL, checking file extension")
                // Check if the URL has a valid audio file extension
                val hasValidExtension = validAudioExtensions.any { extension ->
                    url.lowercase().endsWith(extension)
                }
                
                if (hasValidExtension) {
                    logger.debug("Dropbox URL has valid audio extension")
                    return AudioVerificationResult(
                        isValid = true,
                        mimeType = "audio/mpeg", // Assuming MP3 as default for Dropbox links
                    )
                }
            }

            // Standard verification for other URLs
            HttpClients.createDefault().use { client: CloseableHttpClient ->
                val request = HttpHead(parsedUrl)
                val response = client.execute(request, null) { response -> response }

                val statusCode = response.code
                if (statusCode != HttpStatus.SC_OK) {
                    logger.warn("HTTP error for URL $url: $statusCode")
                    return AudioVerificationResult(
                        isValid = false,
                        error = "HTTP error: $statusCode",
                    )
                }

                val contentType = response.getFirstHeader("Content-Type")?.value
                
                // If content type is application/json but URL is from Dropbox and has dl=1 parameter
                // This is a special case for Dropbox direct download links
                if (contentType?.contains("application/json") == true && 
                    url.contains("dropbox.com") && 
                    (url.contains("dl=1") || url.contains("dl=0"))) {
                    
                    logger.debug("Dropbox URL with application/json content type, checking file extension")
                    val hasValidExtension = validAudioExtensions.any { extension ->
                        url.lowercase().contains(extension)
                    }
                    
                    if (hasValidExtension) {
                        logger.debug("Dropbox URL has valid audio extension")
                        return AudioVerificationResult(
                            isValid = true,
                            mimeType = "audio/mpeg", // Assuming MP3 as default for Dropbox links
                        )
                    }
                }
                
                if (contentType == null || !validAudioMimeTypes.any { contentType.contains(it) }) {
                    logger.warn("Invalid or missing Content-Type for URL $url: $contentType")
                    return AudioVerificationResult(
                        isValid = false,
                        error = "Invalid content type: ${contentType ?: "unknown"}. Expected an audio MIME type.",
                    )
                }

                // Шаг 2: Проверка Content-Length (опционально)
                val contentLength = response.getFirstHeader("Content-Length")?.value?.toLongOrNull()
                if (contentLength != null && contentLength <= 0) {
                    logger.warn("Invalid content length for URL $url: $contentLength")
                    return AudioVerificationResult(
                        isValid = false,
                        error = "Invalid content length: $contentLength",
                    )
                }

                logger.debug("Audio URL $url is valid with content type $contentType")
                AudioVerificationResult(
                    isValid = true,
                    mimeType = contentType,
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to verify URL $url", e)
            AudioVerificationResult(
                isValid = false,
                error = "Failed to verify URL: ${e.message}",
            )
        }
    }
}