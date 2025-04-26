
package com.grooveguess.backend.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.dto.TrackRequest
import com.grooveguess.backend.service.TrackService
import com.grooveguess.backend.service.UserService
import com.grooveguess.backend.service.AudioVerificationResult
import org.junit.jupiter.api.Test
import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.enum.Role
import org.junit.jupiter.api.BeforeEach
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willDoNothing
import org.mockito.BDDMockito.willThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.security.test.context.support.WithMockUser
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.argThat

@WebMvcTest(TrackController::class)
class TrackControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var trackService: TrackService

    @MockitoBean
    lateinit var userService: UserService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private val admin = User(
        id = 42L,
        username = "testuser",
        password = "hashedPassword",
        role = Role.ADMIN,
        email = "test@example.com",
        score = 100
    )
    

    private val sampleTrack = Track(
        id = 1L,
        title = "Test Track",
        artist = "Test Artist",
        url = "http://test.com/audio.mp3"
    )

    private val trackRequest = TrackRequest(
        title = "Test Track",
        artist = "Test Artist",
        url = "http://test.com/audio.mp3"
    )

    @BeforeEach
    fun setup() {
        given(userService.isAdmin(any<Long>())).willReturn(true)
    }

    // @Test
    // @WithMockUser(username = "testuser", roles = ["ADMIN"])
    // fun `createTrack returns CREATED on success`() {
    //     given(userService.isAdmin(eq(42L))).willReturn(true)
    //     given(userService.find(eq(42L))).willReturn(admin)
    //     val createdTrack = Track(
    //         id = 1L,
    //         title = trackRequest.title,
    //         artist = trackRequest.artist,
    //         url = trackRequest.url,
    //         quizzes = mutableListOf()
    //     )
    //     given(trackService.create(
    //         argThat { track ->
    //             track.title == trackRequest.title &&
    //             track.artist == trackRequest.artist &&
    //             track.url == trackRequest.url
    //         },
    //         eq(42L)
    //     )).willReturn(createdTrack)

    //     mockMvc.perform(
    //         post("/api/tracks")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(trackRequest))
    //             .param("creatorId", "42")
    //     )
    //         .andExpect(jsonPath("$.id").value(1))
    //         .andExpect(jsonPath("$.title").value("Test Track"))
    //         .andExpect(jsonPath("$.artist").value("Test Artist"))
    //         .andExpect(jsonPath("$.url").value("http://test.com/audio.mp3"))
    //         .andExpect(status().isCreated)

    // }

    @Test
    fun `createTrack returns FORBIDDEN if not admin`() {
        // Только в этом тесте пользователь не админ
        given(userService.isAdmin(eq(42L))).willReturn(false)

        mockMvc.perform(
            post("/api/tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trackRequest))
                .param("creatorId", "42")
        )
            .andExpect(status().isForbidden)
    }

    // @Test
    // fun `createTrack returns BAD_REQUEST on other errors`() {
    //     given(trackService.create(any(), eq(42L))).willThrow(RuntimeException("Some error"))

    //     mockMvc.perform(
    //         post("/api/tracks")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(trackRequest))
    //             .param("creatorId", "42")
    //     )
    //         .andExpect(status().isBadRequest)
    // }

    // @Test
    // fun `getTrackById returns OK with track`() {
    //     given(trackService.findById(1L)).willReturn(sampleTrack)

    //     mockMvc.perform(get("/api/tracks/1"))
    //         .andExpect(status().isOk)
    //         .andExpect(jsonPath("$.id").value(1))
    //         .andExpect(jsonPath("$.title").value("Test Track"))
    // }

    // @Test
    // fun `getTrackById returns NOT_FOUND if missing`() {
    //     given(trackService.findById(2L)).willThrow(RuntimeException("Not found"))

    //     mockMvc.perform(get("/api/tracks/2"))
    //         .andExpect(status().isNotFound)
    // }

    // @Test
    // fun `getAllTracks returns list of tracks`() {
    //     given(trackService.findAll()).willReturn(listOf(sampleTrack))

    //     mockMvc.perform(get("/api/tracks"))
    //         .andExpect(status().isOk)
    //         .andExpect(jsonPath("$[0].id").value(1))
    //         .andExpect(jsonPath("$[0].title").value("Test Track"))
    // }

    // @Test
    // fun `updateTrack returns OK on success`() {
    //     val updatedTrack = sampleTrack.copy(title = "Updated")
    //     given(trackService.update(eq(1L), any(), eq(42L))).willReturn(updatedTrack)

    //     val updateRequest = TrackRequest("Updated", "Test Artist", "http://test.com/audio.mp3")

    //     mockMvc.perform(
    //         put("/api/tracks/1")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(updateRequest))
    //             .param("userId", "42")
    //     )
    //         .andExpect(status().isOk)
    //         .andExpect(jsonPath("$.title").value("Updated"))
    // }

    // @Test
    // fun `updateTrack returns NOT_FOUND if missing`() {
    //     given(trackService.update(eq(2L), any(), eq(42L))).willReturn(null)

    //     mockMvc.perform(
    //         put("/api/tracks/2")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(trackRequest))
    //             .param("userId", "42")
    //     )
    //         .andExpect(status().isNotFound)
    // }

    @Test
    fun `updateTrack returns FORBIDDEN if not admin`() {
        given(userService.isAdmin(eq(42L))).willReturn(false)

        mockMvc.perform(
            put("/api/tracks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(trackRequest))
                .param("userId", "42")
        )
            .andExpect(status().isForbidden)
    }

    // @Test
    // fun `updateTrack returns BAD_REQUEST on other errors`() {
    //     given(trackService.update(eq(1L), any(), eq(42L))).willThrow(RuntimeException("Some error"))

    //     mockMvc.perform(
    //         put("/api/tracks/1")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(trackRequest))
    //             .param("userId", "42")
    //     )
    //         .andExpect(status().isBadRequest)
    // }

    // @Test
    // fun `deleteTrack returns NO_CONTENT on success`() {
    //     willDoNothing().given(trackService).delete(1L, 42L)

    //     mockMvc.perform(
    //         delete("/api/tracks/1")
    //             .param("userId", "42")
    //     )
    //         .andExpect(status().isNoContent)
    // }

    @Test
    fun `deleteTrack returns FORBIDDEN if not admin`() {
        given(userService.isAdmin(eq(42L))).willReturn(false)

        mockMvc.perform(
            delete("/api/tracks/1")
                .param("userId", "42")
        )
            .andExpect(status().isForbidden)
    }

    // @Test
    // fun `validateAudioUrl returns OK if valid`() {
    //     val result = AudioVerificationResult(isValid = true, mimeType = "audio/mp3", error = null)
    //     given(trackService.verifyAudioUrl("http://test.com/audio.mp3")).willReturn(result)

    //     mockMvc.perform(
    //         get("/api/tracks/validate-audio")
    //             .param("url", "http://test.com/audio.mp3")
    //     )
    //         .andExpect(status().isOk)
    //         .andExpect(jsonPath("$.isValid").value(true))
    //         .andExpect(jsonPath("$.mimeType").value("audio/mp3"))
    // }

    // @Test
    // fun `validateAudioUrl returns BAD_REQUEST if invalid`() {
    //     val result = AudioVerificationResult(isValid = false, mimeType = null, error = "Invalid")
    //     given(trackService.verifyAudioUrl("bad-url")).willReturn(result)

    //     mockMvc.perform(
    //         get("/api/tracks/validate-audio")
    //             .param("url", "bad-url")
    //     )
    //         .andExpect(status().isBadRequest)
    //         .andExpect(jsonPath("$.isValid").value(false))
    //         .andExpect(jsonPath("$.error").value("Invalid"))
    // }
}
