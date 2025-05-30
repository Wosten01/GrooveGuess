package com.grooveguess.backend.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("GrooveGuess API")
                    .version("1.0")
                    .description("API documentation for GrooveGuess backend")
                    .contact(
                        Contact()
                            .name("GrooveGuess Team")
                            .email("support@grooveguess.com")
                    )
            )
    }
}