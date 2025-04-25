package com.grooveguess.backend.domain.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.ManyToMany
import com.grooveguess.backend.domain.model.Quiz
import jakarta.persistence.Column

@Entity
data class Track(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    var id: Long = 0,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val artist: String,

    @Column(nullable = false, unique = true)
    val url: String,

    @ManyToMany(mappedBy = "tracks")
    val quizzes: List<Quiz> = emptyList()
)