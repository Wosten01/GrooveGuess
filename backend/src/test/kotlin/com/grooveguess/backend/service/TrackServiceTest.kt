
package com.grooveguess.backend.service

import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.repository.TrackRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.util.*
import com.grooveguess.backend.domain.enum.Role

class TrackServiceTest {

    @Mock
    private lateinit var trackRepository: TrackRepository

    @Mock
    private lateinit var userService: UserService

    @InjectMocks
    private lateinit var trackService: TrackService

    private lateinit var adminUser: User
    private lateinit var editorUser: User

}
