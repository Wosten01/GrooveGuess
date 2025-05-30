
package com.grooveguess.backend.service

import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.model.Quiz
import com.grooveguess.backend.domain.repository.TrackRepository
import com.grooveguess.backend.domain.repository.QuizRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.mockito.BDDMockito.*
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Sort
import java.util.*

class TrackServiceTest {

    private val trackRepository: TrackRepository = mock()
    private val trackService: TrackService = mock()
    private val userService: UserService = mock()
    private val quizRepository: QuizRepository = mock()
    private val service = TrackService(trackRepository, userService, quizRepository)

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
    fun `findAll returns all tracks with pagination`() {
        // Create a PageRequest with the same sorting as used in the service
        val sort = Sort.by(Sort.Direction.DESC, "id")
        val pageable = PageRequest.of(0, 2, sort)
        
        val track1 = Track(id = 1, title = "A", artist = "B", url = "url1")
        val track2 = Track(id = 2, title = "C", artist = "D", url = "url2")
        
        // Use doReturn().when() syntax to avoid strict stubbing issues
        doReturn(PageImpl(listOf(track1, track2), pageable, 2))
            .`when`(trackRepository).findAll(pageable)

        val result = service.findAll(0, 2)

        assertEquals(2, result.content.size)
        assertEquals(track1.title, result.content[0].title)
        assertEquals(track2.title, result.content[1].title)
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
        
        // Mock the track with an empty list of quizzes
        val trackWithEmptyQuizzes = sampleTrack.copy(quizzes = emptyList())
        given(trackRepository.findById(1L)).willReturn(Optional.of(trackWithEmptyQuizzes))
        
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
        val result = service.verifyAudioUrl("https://file-examples.com/storage/fe7d258bd9680a7429c6b40/2017/11/file_example_MP3_700KB.mp3")
        assertNotNull(result)
    }

    @Test
    fun `verifyAudioUrl returns invalid for bad url`() {
        val result = service.verifyAudioUrl("not-a-url")
        assertFalse(result.isValid)
        assertNotNull(result.error)
    }
}
