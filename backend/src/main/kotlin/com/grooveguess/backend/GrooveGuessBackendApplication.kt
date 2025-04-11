package com.grooveguess.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GrooveGuessBackendApplication

fun main(args: Array<String>) {
	runApplication<GrooveGuessBackendApplication>(*args)
}
