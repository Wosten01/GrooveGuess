package com.grooveguess.domain.repository

import com.grooveguess.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>