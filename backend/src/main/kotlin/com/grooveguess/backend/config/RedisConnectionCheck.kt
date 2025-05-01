package com.grooveguess.backend.config

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct

@Component
class RedisConnectionCheck(private val redisTemplate: RedisTemplate<String, Any>) {
    
    private val logger = LoggerFactory.getLogger(RedisConnectionCheck::class.java)
    
    @PostConstruct
    fun checkConnection() {
        try {
            logger.info("Testing Redis connection...")
            val testKey = "test:connection"
            val testValue = "Connection test at ${System.currentTimeMillis()}"
            
            // Try to write to Redis
            redisTemplate.opsForValue().set(testKey, testValue)
            logger.info("Successfully wrote test value to Redis")
            
            // Try to read from Redis
            val retrievedValue = redisTemplate.opsForValue().get(testKey)
            logger.info("Retrieved value from Redis: $retrievedValue")
            
            if (testValue == retrievedValue) {
                logger.info("Redis connection test PASSED ✓")
            } else {
                logger.error("Redis connection test FAILED: Retrieved value doesn't match written value")
            }
            
            // Clean up
            redisTemplate.delete(testKey)
            
        } catch (e: Exception) {
            logger.error("Redis connection test FAILED with exception", e)
        }
    }
}