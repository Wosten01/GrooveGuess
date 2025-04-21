package com.grooveguess.backend.security

import com.grooveguess.backend.domain.model.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.Claims
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Component
class JwtUtil {
    private val jwtSecret = "your-very-secret-key"
    private val jwtExpirationMs = 5 * 60 * 1000 // 5 minutes
    // private val jwtExpirationMs = 24 * 60 * 60 * 1000 // 24 hours
    private val key = SecretKeySpec(jwtSecret.toByteArray(), SignatureAlgorithm.HS256.jcaName)

    fun generateToken(user: User): String {
        val claims = Jwts.claims().apply {
            subject = user.id?.toString() ?: ""
            this["role"] = user.role.name
            this["email"] = user.email
        }
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getUserIdFromToken(token: String): Long? {
        return try {
            val claims = getClaims(token)
            claims.subject.toLongOrNull()
        } catch (ex: Exception) {
            null
        }
    }

    fun getRoleFromToken(token: String): String? {
        return try {
            val claims = getClaims(token)
            claims["role"] as? String
        } catch (ex: Exception) {
            null
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            !claims.expiration.before(Date())
        } catch (ex: Exception) {
            false
        }
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }
}