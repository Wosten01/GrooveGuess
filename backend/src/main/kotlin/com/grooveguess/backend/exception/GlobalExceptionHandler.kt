package com.grooveguess.backend.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    // Обработка исключений доступа
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.warn("Access denied: ${ex.message}")
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.FORBIDDEN.value(),
            error = "Forbidden",
            message = ex.message ?: "You don't have access to this session",
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }
    
    // Обработка исключений доступа (IllegalAccessException)
    @ExceptionHandler(IllegalAccessException::class)
    fun handleIllegalAccessException(ex: IllegalAccessException, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.warn("Access denied: ${ex.message}")
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.FORBIDDEN.value(),
            error = "Forbidden",
            message = "You don't have access to this session",
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }
    
    // Обработка исключений "ресурс не найден"
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.warn("Resource not found: ${ex.message}")
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = ex.message ?: "Resource not found",
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
    
    // Обработка исключений валидации
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid argument: ${ex.message}")
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Invalid argument",
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    // Обработка исключений состояния
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.warn("Invalid state: ${ex.message}")
        
        // Специальная обработка для определенных сообщений
        val status = when (ex.message) {
            "No more rounds available" -> HttpStatus.NO_CONTENT
            "Game is already completed" -> HttpStatus.CONFLICT
            "Session not found" -> HttpStatus.NOT_FOUND
            else -> HttpStatus.BAD_REQUEST
        }
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = status.value(),
            error = status.reasonPhrase,
            message = ex.message ?: "Invalid state",
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity.status(status).body(errorResponse)
    }
    
    // Обработка RuntimeException
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.error("Runtime error: ${ex.message}", ex)
        
        // Определяем статус на основе сообщения об ошибке
        val status = when {
            ex.message?.contains("not found", ignoreCase = true) == true -> HttpStatus.NOT_FOUND
            ex.message?.contains("invalid", ignoreCase = true) == true -> HttpStatus.BAD_REQUEST
            ex.message?.contains("access", ignoreCase = true) == true -> HttpStatus.FORBIDDEN
            else -> HttpStatus.BAD_REQUEST
        }
        
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = status.value(),
            error = status.reasonPhrase,
            message = ex.message ?: "An error occurred",
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity.status(status).body(errorResponse)
    }
    
    // Обработка всех остальных исключений
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error: ${ex.message}", ex)
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred",
            path = request.getDescription(false).substringAfter("uri=")
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}

// Единый формат ответа об ошибке
data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)