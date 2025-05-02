package com.grooveguess.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.databind.DeserializationFeature
import com.grooveguess.backend.domain.model.GameSession
import org.springframework.stereotype.Component

@Configuration
class RedisConfig {

    @Value("\${spring.redis.host:localhost}")
    private lateinit var redisHost: String

    @Value("\${spring.redis.port:6379}")
    private var redisPort: Int = 6379

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration(redisHost, redisPort)
        return LettuceConnectionFactory(config)
    }

    @Bean
    fun jacksonObjectMapper(): ObjectMapper {
        return ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = redisConnectionFactory
        
        val objectMapper = jacksonObjectMapper()
        
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        
        return template
    }
    
}

@Component
class RedisUtils(private val objectMapper: ObjectMapper) {
    
    fun <T> convertMapToObject(map: Map<*, *>, targetClass: Class<T>): T {
        val json = objectMapper.writeValueAsString(map)
        return objectMapper.readValue(json, targetClass)
    }
}