package com.grooveguess.backend.util

import com.grooveguess.backend.domain.model.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.Claims
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Value
import java.util.*
import java.nio.charset.StandardCharsets
import javax.crypto.spec.SecretKeySpec

@Component
class JwtUtil(
    @Value("\${jwt.secret}") private val jwtSecret: String,
    @Value("\${jwt.expirationMs}") private val jwtExpirationMs: Long
) {
    private val key  = SecretKeySpec(jwtSecret.toByteArray(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.jcaName)
    
    fun generateToken(user: User): String {
        val claims = Jwts.claims().apply {
            this["role"] = user.role.name
            this["score"] = user.score
        }

        val issuer = user.email

        return Jwts.builder()
            .setClaims(claims)
            .setSubject("GrooveGuess-backend")
            .setIssuer(issuer)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + jwtExpirationMs))
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

    fun getIssuerFromToken(token: String): Long? {
        return try {
            val claims = getClaims(token)
            claims.issuer.toLong()
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