// package com.grooveguess.domain.repository

// import com.grooveguess.domain.model.Track
// import org.junit.jupiter.api.Test
// import org.springframework.beans.factory.annotation.Autowired
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
// import org.junit.jupiter.api.Assertions.assertEquals
// import org.junit.jupiter.api.Assertions.assertNotNull

// @DataJpaTest
// class TrackRepositoryTest {

//     @Autowired
//     private lateinit var trackRepository: TrackRepository

//     @Test
//     fun `should save and retrieve track`() {
//         val track = Track(
//             title = "Song",
//             artist = "Artist",
//             url = "http://example.com/song.mp3"
//         )
//         val savedTrack = trackRepository.save(track)

//         val foundTrack = trackRepository.findById(savedTrack.id)

//         assertNotNull(foundTrack)
//         assertEquals(savedTrack, foundTrack.get())
//         assertEquals("Song", foundTrack.get().title)
//     }
// }