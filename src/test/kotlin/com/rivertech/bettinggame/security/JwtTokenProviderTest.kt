package com.rivertech.bettinggame.security

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var jwtProperties: JwtProperties
    private lateinit var authentication: Authentication

    @BeforeEach
    fun setUp() {
        jwtProperties = mockk {
            every { secret } returns Base64.getEncoder().encodeToString("test-secret-key-should-be-at-least-32-bytes-long".toByteArray())
            every { expirationMs } returns 3600000L
        }
        jwtTokenProvider = JwtTokenProvider(jwtProperties)
        authentication = mockk {
            every { principal } returns User("testuser", "password", listOf(SimpleGrantedAuthority("ROLE_USER")))
        }
    }

    @Test
    fun `generateToken should return a valid JWT`() {
        val token = jwtTokenProvider.generateToken(authentication)
        assertNotNull(token)
    }

    @Test
    fun `validateToken should return true for valid token`() {
        val token = jwtTokenProvider.generateToken(authentication)
        assertTrue(jwtTokenProvider.validateToken(token))
    }

    @Test
    fun `validateToken should return false for invalid token`() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"))
    }

    @Test
    fun `getUsernameFromToken should return correct username`() {
        val token = jwtTokenProvider.generateToken(authentication)
        val username = jwtTokenProvider.getUsernameFromToken(token)
        assertNotNull(username)
        assert(username == "testuser")
    }

}
