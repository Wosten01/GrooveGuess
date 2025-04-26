
package com.grooveguess.backend.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.model.User
import com.grooveguess.backend.domain.enum.Role
import com.grooveguess.backend.service.AudioVerificationResult
import com.grooveguess.backend.service.TrackService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.context.bean.override.mockito.MockitoBean

@WebMvcTest(TrackController::class)
class TrackControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var trackService: TrackService

    @Autowired
    lateinit var objectMapper: ObjectMapper
}
