package com.grooveguess.backend.domain.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Column
import com.grooveguess.backend.domain.model.Track

@Entity
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val username: String,
    @Column(unique = true)
    val email: String,
    val password: String,
    val role: String = "USER",
    val score: Int = 0
)