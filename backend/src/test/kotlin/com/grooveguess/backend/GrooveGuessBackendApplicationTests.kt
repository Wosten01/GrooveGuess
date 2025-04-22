package com.grooveguess.backend

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

@SpringBootTest
class GrooveGuessBackendApplicationTests {
	@Autowired
    private lateinit var context: ApplicationContext

    @Test
    fun contextLoads() {
        context.beanDefinitionNames.forEach { println("Bean: $it") }
    }
}
