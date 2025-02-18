package com.rivertech.bettinggame.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.Claims
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import java.util.*

@Component
class JwtTokenProvider(private val jwtProperties: JwtProperties) {
    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    private val key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtProperties.secret))

    fun generateToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as org.springframework.security.core.userdetails.User
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.expirationMs)

        return Jwts.builder()
            .setSubject(userPrincipal.username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .claim("roles", userPrincipal.authorities.map { it.authority })
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            parseClaims(token)
            true
        } catch (e: Exception) {
            logger.error("Invalid JWT Token", e)
            false
        }
    }

    fun getUsernameFromToken(token: String): String = parseClaims(token).subject

    private fun parseClaims(token: String): Claims {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
    }
}

@Component
class JwtProperties {
    val expirationMs: Long = System.getenv("JWT_EXPIRATION_MS")?.toLong() ?: 3600000L
    val secret: String = System.getenv("JWT_SECRET") ?: "dGVzdC1zZWNyZXQtc2hvdWxkLWJlLWF0LWxlYXN0LTMyLWJ5dGVzLWxvbmc="
}
