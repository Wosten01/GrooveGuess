package com.grooveguess.backend.domain.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Column
import jakarta.persistence.Enumerated
import jakarta.persistence.EnumType
import com.grooveguess.backend.domain.model.Track
import com.grooveguess.backend.domain.enum.Role



@Entity
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long? = null,
    val username: String,
    @Column(unique = true)
    val email: String,
    val password: String,
    @Enumerated(EnumType.STRING)
    val role: Role = Role.USER,
    val score: Int = 0
)