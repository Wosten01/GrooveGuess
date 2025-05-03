package com.grooveguess.backend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.util.StringUtils
import com.grooveguess.backend.util.JwtUtil
import com.grooveguess.backend.service.UserService

@Component
class JwtAuthenticationFilter(
    private val jwtUtils: JwtUtil,
    private val userService: UserService
) : OncePerRequestFilter() {


    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = getJwtFromRequest(request)
            
            if (jwt == null) {
                logger.debug("No JWT token found in request")
            } else if (!jwtUtils.validateToken(jwt)) {
                logger.warn("Invalid JWT token")
            } else {
                val id = jwtUtils.getUserIdFromToken(jwt)

                if (id == null) {
                    logger.warn("Could not get user ID from token")
                } else {
                    val user = userService.findById(id)
                    
                    if (user == null) {
                        logger.warn("User not found with ID: $id")
                    } else {
                        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
                        
                        val authentication = UsernamePasswordAuthenticationToken(
                            user, null, authorities
                        )
                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                        SecurityContextHolder.getContext().authentication = authentication
                        
                        logger.info("Authentication set successfully for user: ${user.id}")
                        
                        val currentAuth = SecurityContextHolder.getContext().authentication
                        if (currentAuth != null) {
                            logger.debug("Current authentication: ${currentAuth.name}, authorities: ${currentAuth.authorities}")
                        } else {
                            logger.warn("Authentication was set but is now null!")
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            logger.error("Could not set user authentication in security context", ex)
        }
        
        filterChain.doFilter(request, response)
        
        SecurityContextHolder.getContext().authentication
    }
    
    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            logger.debug("JWT found in Authorization header")
            return bearerToken.substring(7)
        }

        return null
    }
}