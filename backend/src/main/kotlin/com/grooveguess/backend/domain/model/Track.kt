package com.grooveguess.backend.domain.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.ManyToMany
import com.grooveguess.backend.domain.model.Quiz

@Entity
data class Track(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val title: String,
    val artist: String,
    val url: String,
    @ManyToMany(mappedBy = "tracks")
    val quizzes: List<Quiz> = emptyList()
)