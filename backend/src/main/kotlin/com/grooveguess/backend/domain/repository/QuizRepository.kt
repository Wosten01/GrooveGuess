package com.grooveguess.domain.repository

import com.grooveguess.domain.model.Quiz
import org.springframework.data.jpa.repository.JpaRepository

interface QuizRepository : JpaRepository<Quiz, Long>