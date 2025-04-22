
package com.grooveguess.backend.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component


@Component
class CookieUtils {
    data class CookieParams(
        val name: String,
        val value: String,
        val path: String = "/",
        val maxAge: Int = -1,
        val secure: Boolean = false,
        val httpOnly: Boolean = true,
        val sameSite: String = "Lax"
    )
    fun addCookie(
        response: HttpServletResponse,
        name: String,
        value: String,
        path: String = "/",
        maxAge: Int = -1,
        secure: Boolean = false,
        httpOnly: Boolean = true,
        sameSite: String = "Lax"
    ) {
        val cookie = Cookie(name, value)
        cookie.path = path
        cookie.maxAge = maxAge
        cookie.secure = secure
        cookie.isHttpOnly = httpOnly
        val cookieHeader = buildString {
            append("${cookie.name}=${cookie.value}; Path=${cookie.path}")
            if (cookie.maxAge >= 0) append("; Max-Age=${cookie.maxAge}")
            if (cookie.secure) append("; Secure")
            if (cookie.isHttpOnly) append("; HttpOnly")
            if (sameSite.isNotBlank()) append("; SameSite=$sameSite")
        }
        response.addHeader("Set-Cookie", cookieHeader)
    }
    fun addCookies(
        response: HttpServletResponse,
        cookies: List<CookieParams>
    ) {
        for (params in cookies) {
            addCookie(
                response = response,
                name = params.name,
                value = params.value,
                path = params.path,
                maxAge = params.maxAge,
                secure = params.secure,
                httpOnly = params.httpOnly,
                sameSite = params.sameSite
            )
        }
    }
    
    fun addJwtCookie(
        response: HttpServletResponse,
        jwt: String,
        path: String = "/",
        maxAge: Int = -1,
        secure: Boolean = false,
        httpOnly: Boolean = true,
        sameSite: String = "Lax"
    ) {
        addCookie(
            response = response,
            name = "jwt",
            value = jwt,
            path = path,
            maxAge = maxAge,
            secure = secure,
            httpOnly = httpOnly,
            sameSite = sameSite
        )
    }
}
