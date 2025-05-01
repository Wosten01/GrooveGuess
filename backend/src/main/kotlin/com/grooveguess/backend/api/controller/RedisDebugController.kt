package com.grooveguess.backend.api.controller

import com.grooveguess.backend.service.QuizGameService
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/debug/redis")
class RedisDebugController(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val quizGameService: QuizGameService
) {
    private val logger = LoggerFactory.getLogger(RedisDebugController::class.java)
    
    @GetMapping("/ping")
    fun pingRedis(): Map<String, Any> {
        logger.info("Testing Redis connection with PING")
        val result = mutableMapOf<String, Any>()
        
        try {
            val pingResult = redisTemplate.execute { connection ->
                connection.ping()
            }
            result["status"] = "success"
            result["ping"] = pingResult?.toString() ?: "null"
            logger.info("Redis PING successful: $pingResult")
        } catch (e: Exception) {
            result["status"] = "error"
            result["error"] = e.message ?: "Unknown error"
            result["errorType"] = e.javaClass.simpleName
            logger.error("Redis PING failed", e)
        }
        
        return result
    }
    
    @GetMapping("/keys")
    fun getAllKeys(): Map<String, Any> {
        logger.info("Retrieving all Redis keys")
        val result = mutableMapOf<String, Any>()
        
        try {
            val keys = redisTemplate.keys("*") ?: emptySet()
            result["status"] = "success"
            result["keyCount"] = keys.size
            result["keys"] = keys.toList()
            logger.info("Retrieved ${keys.size} Redis keys")
        } catch (e: Exception) {
            result["status"] = "error"
            result["error"] = e.message ?: "Unknown error"
            result["errorType"] = e.javaClass.simpleName
            logger.error("Failed to retrieve Redis keys", e)
        }
        
        return result
    }
    
    @GetMapping("/sessions")
    fun getSessionKeys(): Map<String, Any> {
        logger.info("Retrieving Redis session keys")
        val result = mutableMapOf<String, Any>()
        
        try {
            val keys = redisTemplate.keys("quiz_session:*") ?: emptySet()
            result["status"] = "success"
            result["sessionCount"] = keys.size
            result["sessionKeys"] = keys.toList()
            logger.info("Retrieved ${keys.size} Redis session keys")
        } catch (e: Exception) {
            result["status"] = "error"
            result["error"] = e.message ?: "Unknown error"
            result["errorType"] = e.javaClass.simpleName
            logger.error("Failed to retrieve Redis session keys", e)
        }
        
        return result
    }
    
    @GetMapping("/session/{sessionId}")
    fun getSessionData(@PathVariable sessionId: String): Map<String, Any> {
        logger.info("Retrieving data for session: $sessionId")
        val result = mutableMapOf<String, Any>()
        
        try {
            val sessionKey = "quiz_session:$sessionId"
            val sessionData = redisTemplate.opsForValue().get(sessionKey)
            
            if (sessionData != null) {
                result["status"] = "success"
                result["sessionKey"] = sessionKey
                result["sessionData"] = sessionData
                logger.info("Successfully retrieved data for session: $sessionId")
            } else {
                result["status"] = "not_found"
                result["message"] = "No data found for session: $sessionId"
                logger.warn("No data found for session: $sessionId")
            }
        } catch (e: Exception) {
            result["status"] = "error"
            result["error"] = e.message ?: "Unknown error"
            result["errorType"] = e.javaClass.simpleName
            logger.error("Error retrieving session data", e)
        }
        
        return result
    }
    
    @GetMapping("/test-write")
    fun testRedisWrite(): Map<String, Any> {
        logger.info("Testing Redis write operation")
        val result = mutableMapOf<String, Any>()
        
        try {
            val testKey = "test:write:${UUID.randomUUID()}"
            val testValue = "Test value at ${System.currentTimeMillis()}"
            
            // Write to Redis
            redisTemplate.opsForValue().set(testKey, testValue)
            logger.info("Successfully wrote test value to Redis")
            
            // Read from Redis
            val retrievedValue = redisTemplate.opsForValue().get(testKey)
            logger.info("Retrieved value from Redis: $retrievedValue")
            
            result["status"] = "success"
            result["testKey"] = testKey
            result["writtenValue"] = testValue
            result["retrievedValue"] = retrievedValue ?: "null"
            result["valueMatch"] = testValue == retrievedValue
            
            // Clean up
            redisTemplate.delete(testKey)
            logger.info("Deleted test key from Redis")
            
        } catch (e: Exception) {
            result["status"] = "error"
            result["error"] = e.message ?: "Unknown error"
            result["errorType"] = e.javaClass.simpleName
            logger.error("Redis write test failed", e)
        }
        
        return result
    }
}