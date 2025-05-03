
package com.grooveguess.backend.domain.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "quizzes")
data class Quiz(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val title: String,
    val description: String?,
    val roundCount: Int,

    @ManyToMany
    @JoinTable(
        name = "quiz_tracks",
        joinColumns = [JoinColumn(name = "quiz_id")],
        inverseJoinColumns = [JoinColumn(name = "track_id")]
    )
    val tracks: List<Track> = emptyList(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    val creator: User,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    override fun toString(): String {
        return "Quiz(id=$id, title='$title', description='$description', roundCount=$roundCount, creatorId=${creator.id}, tracksCount=${tracks.size}, createdAt=$createdAt)"
    }
}
