package com.grooveguess.domain.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany

@Entity
data class Quiz(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val title: String,
    val description: String?,
    val roundCount: Int,
    @ManyToMany
    val tracks: List<Track> = emptyList(),
)