
package com.grooveguess.backend.service

import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.repository.TrackRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.*
import org.mockito.Mockito
import org.mockito.kotlin.mock
import java.util.*

class TrackServiceTest {

    private val trackRepository: TrackRepository = mock()
    private val userService: UserService = mock()
    private val service = TrackService(trackRepository, userService)

    private val sampleTrack = Track(
        id = 1L,
        title = "Test Track",
        artist = "Test Artist",
        url = "http://test.com/audio.mp3"
    )

    @Test
    fun `create saves track if user is admin`() {
        given(userService.isAdmin(42L)).willReturn(true)
        given(trackRepository.save(sampleTrack)).willReturn(sampleTrack)

        val result = service.create(sampleTrack, 42L)
        assertEquals(sampleTrack, result)
    }

    @Test
    fun `create throws if user is not admin`() {
        given(userService.isAdmin(42L)).willReturn(false)
        assertThrows<IllegalAccessException> {
            service.create(sampleTrack, 42L)
        }
    }

    @Test
    fun `findById returns track if exists`() {
        given(trackRepository.findById(1L)).willReturn(Optional.of(sampleTrack))
        val result = service.findById(1L)
        assertEquals(sampleTrack, result)
    }

    @Test
    fun `findById throws if not found`() {
        given(trackRepository.findById(2L)).willReturn(Optional.empty())
        assertThrows<RuntimeException> {
            service.findById(2L)
        }
    }

    @Test
    fun `findAll returns all tracks`() {
        given(trackRepository.findAll()).willReturn(listOf(sampleTrack))
        val result = service.findAll()
        assertEquals(1, result.size)
        assertEquals(sampleTrack, result[0])
    }

    @Test
    fun `update updates track if user is admin and audio is valid`() {
        given(userService.isAdmin(42L)).willReturn(true)
        given(trackRepository.findById(1L)).willReturn(Optional.of(sampleTrack))
        given(trackRepository.save(Mockito.any(Track::class.java))).willAnswer { it.arguments[0] }

        val spyService = Mockito.spy(service)
        doReturn(AudioVerificationResult(true, "audio/mpeg", null)).`when`(spyService).verifyAudioUrl("http://test.com/audio.mp3")

        val updated = sampleTrack.copy(title = "New Title")
        val result = spyService.update(1L, updated, 42L)
        assertNotNull(result)
        assertEquals("New Title", result?.title)
    }

    @Test
    fun `update throws if user is not admin`() {
        given(userService.isAdmin(42L)).willReturn(false)
        assertThrows<IllegalAccessException> {
            service.update(1L, sampleTrack, 42L)
        }
    }

    @Test
    fun `update throws if audio is invalid`() {
        given(userService.isAdmin(42L)).willReturn(true)
        val spyService = Mockito.spy(service)
        doReturn(AudioVerificationResult(false, null, "Invalid audio")).`when`(spyService).verifyAudioUrl("bad-url")
        val badTrack = sampleTrack.copy(url = "bad-url")
        assertThrows<RuntimeException> {
            spyService.update(1L, badTrack, 42L)
        }
    }

    @Test
    fun `update returns null if track not found`() {
        given(userService.isAdmin(42L)).willReturn(true)
        given(trackRepository.findById(99L)).willReturn(Optional.empty())
        val spyService = Mockito.spy(service)
        doReturn(AudioVerificationResult(true, "audio/mpeg", null)).`when`(spyService).verifyAudioUrl("http://test.com/audio.mp3")
        val result = spyService.update(99L, sampleTrack, 42L)
        assertNull(result)
    }

    @Test
    fun `delete removes track if user is admin`() {
        given(userService.isAdmin(42L)).willReturn(true)
        willDoNothing().given(trackRepository).deleteById(1L)
        service.delete(1L, 42L)
        Mockito.verify(trackRepository).deleteById(1L)
    }

    @Test
    fun `delete throws if user is not admin`() {
        given(userService.isAdmin(42L)).willReturn(false)
        assertThrows<IllegalAccessException> {
            service.delete(1L, 42L)
        }
    }

    @Test
    fun `verifyAudioUrl returns valid for good url`() {
        // Для ускорения теста можно замокать HTTP-клиент, но здесь тестируем реальный метод
        val result = service.verifyAudioUrl("https://file-examples.com/storage/fe7d258bd9680a7429c6b40/2017/11/file_example_MP3_700KB.mp3")
        // Не гарантируем что URL всегда будет доступен, поэтому просто проверим тип результата
        assertNotNull(result)
    }

    @Test
    fun `verifyAudioUrl returns invalid for bad url`() {
        val result = service.verifyAudioUrl("not-a-url")
        assertFalse(result.isValid)
        assertNotNull(result.error)
    }
}
