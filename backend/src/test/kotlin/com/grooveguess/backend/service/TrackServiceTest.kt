package com.grooveguess.backend.service

import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.repository.TrackRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import java.util.Optional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

@ExtendWith(MockitoExtension::class)
class TrackServiceTest {

    @Mock
    private lateinit var trackRepository: TrackRepository

    @Mock
    private lateinit var userService: UserService

    @InjectMocks
    private lateinit var trackService: TrackService

    @Test
    fun `create should save track when user is admin`() {
        val track = Track(id = 0, title = "Song", artist = "Artist", url = "http://example.com")
        val savedTrack = track.copy(id = 1)
        whenever(userService.isAdmin(1)).thenReturn(true)
        whenever(trackRepository.save(track)).thenReturn(savedTrack)

        val result = trackService.create(track, 1)

        assertEquals(savedTrack, result)
        verify(trackRepository).save(track)
    }

    @Test
    fun `create should throw exception when user is not admin`() {
        val track = Track(id = 0, title = "Song", artist = "Artist", url = "http://example.com")
        whenever(userService.isAdmin(1)).thenReturn(false)

        val exception = assertThrows<IllegalAccessException> {
            trackService.create(track, 1)
        }

        assertEquals("Only admins can create tracks", exception.message)
    }
}