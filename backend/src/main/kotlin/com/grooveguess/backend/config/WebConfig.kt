package com.grooveguess.backend.config

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.context.annotation.Configuration

@Configuration
@EnableWebMvc
class WebConfig: WebMvcConfigurer {


    override fun addCorsMappings(registry: CorsRegistry){
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080", "http://localhost:5137")
                .allowCredentials(true)
    }

}

