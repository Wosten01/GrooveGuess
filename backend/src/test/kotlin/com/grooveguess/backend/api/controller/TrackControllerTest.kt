package com.grooveguess.backend.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.grooveguess.backend.domain.dto.TrackDto
import com.grooveguess.backend.domain.dto.TrackRequest
import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.exception.AccessDeniedException
import com.grooveguess.backend.service.AudioVerificationResult
import com.grooveguess.backend.service.TrackService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.context.bean.override.mockito.MockitoBean

@WebMvcTest(TrackController::class)
@AutoConfigureMockMvc(addFilters = false)
class TrackControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var trackService: TrackService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `createTrack should return created track when successful`() {
        val creatorId = 1L
        val trackRequest = TrackRequest(
            title = "Test Track",
            artist = "Test Artist",
            url = "http://example.com/audio.mp3"
        )
        
        val createdTrack = Track(
            id = 1L,
            title = trackRequest.title,
            artist = trackRequest.artist,
            url = trackRequest.url
        )

        `when`(trackService.create(any(), eq(creatorId))).thenReturn(createdTrack)

        mockMvc.perform(post("/api/tracks")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trackRequest))
                .param("creatorId", creatorId.toString()))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Test Track"))
            .andExpect(jsonPath("$.artist").value("Test Artist"))
            .andExpect(jsonPath("$.url").value("http://example.com/audio.mp3"))
    }


    @Test
    fun `createTrack should return bad request on error`() {
        val creatorId = 1L
        val trackRequest = TrackRequest(
            title = "Test Track",
            artist = "Test Artist",
            url = "invalid-url"
        )

        `when`(trackService.create(any(), eq(creatorId))).thenThrow(RuntimeException("Invalid URL"))

        mockMvc.perform(post("/api/tracks")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trackRequest))
                .param("creatorId", creatorId.toString()))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `getTrackById should return track when found`() {
        val trackId = 1L
        val track = Track(
            id = trackId,
            title = "Test Track",
            artist = "Test Artist",
            url = "http://example.com/audio.mp3"
        )

        `when`(trackService.findById(trackId)).thenReturn(track)

        mockMvc.perform(get("/api/tracks/$trackId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(trackId))
            .andExpect(jsonPath("$.title").value("Test Track"))
            .andExpect(jsonPath("$.artist").value("Test Artist"))
            .andExpect(jsonPath("$.url").value("http://example.com/audio.mp3"))
    }

    @Test
    fun `getTrackById should return not found when track does not exist`() {
        val trackId = 999L

        `when`(trackService.findById(trackId)).thenThrow(RuntimeException("Track not found"))

        mockMvc.perform(get("/api/tracks/$trackId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `getAllTracks should return page of tracks`() {
        val page = 0
        val size = 20
        val search = ""
        
        val tracks = listOf(
            TrackDto(1L, "Track 1", "Artist 1", "http://example.com/track1.mp3"),
            TrackDto(2L, "Track 2", "Artist 2", "http://example.com/track2.mp3")
        )
        
        val trackPage = PageImpl(tracks, PageRequest.of(page, size), tracks.size.toLong())

        `when`(trackService.findAll(page, size, search)).thenReturn(trackPage)

        mockMvc.perform(get("/api/tracks")
                .with(csrf())
                .param("page", page.toString())
                .param("size", size.toString())
                .param("search", search)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].title").value("Track 1"))
            .andExpect(jsonPath("$.content[1].id").value(2))
            .andExpect(jsonPath("$.content[1].title").value("Track 2"))
    }

    @Test
    fun `updateTrack should return updated track when successful`() {
        val trackId = 1L
        val userId = 1L
        val trackRequest = TrackRequest(
            title = "Updated Track",
            artist = "Test Artist",
            url = "http://example.com/audio.mp3"
        )
        
        val updatedTrack = Track(
            id = trackId,
            title = trackRequest.title,
            artist = trackRequest.artist,
            url = trackRequest.url
        )

        `when`(trackService.update(eq(trackId), any(), eq(userId))).thenReturn(updatedTrack)

        mockMvc.perform(put("/api/tracks/$trackId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trackRequest))
                .param("userId", userId.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(trackId))
            .andExpect(jsonPath("$.title").value("Updated Track"))
            .andExpect(jsonPath("$.artist").value("Test Artist"))
            .andExpect(jsonPath("$.url").value("http://example.com/audio.mp3"))
    }

    @Test
    fun `updateTrack should return not found when track does not exist`() {
        val trackId = 999L
        val userId = 1L
        val trackRequest = TrackRequest(
            title = "Updated Track",
            artist = "Test Artist",
            url = "http://example.com/audio.mp3"
        )

        `when`(trackService.update(eq(trackId), any(), eq(userId))).thenReturn(null)

        mockMvc.perform(put("/api/tracks/$trackId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trackRequest))
                .param("userId", userId.toString()))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `updateTrack should return bad request on error`() {
        val trackId = 1L
        val userId = 1L
        val trackRequest = TrackRequest(
            title = "Updated Track",
            artist = "Test Artist",
            url = "invalid-url"
        )

        `when`(trackService.update(eq(trackId), any(), eq(userId))).thenThrow(RuntimeException("Invalid URL"))

        mockMvc.perform(put("/api/tracks/$trackId")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trackRequest))
                .param("userId", userId.toString()))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `deleteTrack should return no content when successful`() {
        val trackId = 1L
        val userId = 1L

        doNothing().`when`(trackService).delete(trackId, userId)

        mockMvc.perform(delete("/api/tracks/$trackId")
                .with(csrf())
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent)
    }


    @Test
    fun `validateAudioUrl should return ok for valid url`() {
        val url = "http://example.com/audio.mp3"
        val result = AudioVerificationResult(
            isValid = true,
            mimeType = "audio/mpeg",
            error = null
        )

        `when`(trackService.verifyAudioUrl(url)).thenReturn(result)

        mockMvc.perform(get("/api/tracks/validate-audio")
                .with(csrf())
                .param("url", url)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isValid").value(true))
            .andExpect(jsonPath("$.mimeType").value("audio/mpeg"))
            .andExpect(jsonPath("$.error").doesNotExist())
    }

    @Test
    fun `validateAudioUrl should return bad request for invalid url`() {
        val url = "invalid-url"
        val result = AudioVerificationResult(
            isValid = false,
            mimeType = null,
            error = "Invalid URL format"
        )

        `when`(trackService.verifyAudioUrl(url)).thenReturn(result)

        mockMvc.perform(get("/api/tracks/validate-audio")
                .with(csrf())
                .param("url", url)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.isValid").value(false))
            .andExpect(jsonPath("$.mimeType").doesNotExist())
            .andExpect(jsonPath("$.error").value("Invalid URL format"))
    }
}