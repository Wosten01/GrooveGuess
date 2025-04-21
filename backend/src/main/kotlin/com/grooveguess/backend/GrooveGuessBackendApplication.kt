package com.grooveguess.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
class GrooveGuessBackendApplication

fun main(args: Array<String>) {
	runApplication<GrooveGuessBackendApplication>(*args)
}
