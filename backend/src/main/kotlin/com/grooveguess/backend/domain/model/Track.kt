package com.grooveguess.backend.domain.model

import jakarta.persistence.*
import com.grooveguess.backend.domain.model.Quiz
import com.grooveguess.backend.domain.model.User

@Entity
@Table(name = "tracks")
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
    val quizzes: List<Quiz> = emptyList(),
) {
    override fun toString(): String {
        return "Track(id=$id, title='$title', artist='$artist', url='$url', quizzesCount=${quizzes.size})"
    }
}
