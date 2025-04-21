package com.grooveguess.backend.domain.repository

import com.grooveguess.backend.domain.model.Quiz
import org.springframework.data.jpa.repository.JpaRepository

interface QuizRepository : JpaRepository<Quiz, Long>