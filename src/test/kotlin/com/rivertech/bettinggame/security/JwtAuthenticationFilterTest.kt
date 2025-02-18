package com.rivertech.bettinggame.security

import io.mockk.*
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JwtAuthenticationFilterTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var userDetailsService: UserDetailsService
    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter
    private lateinit var request: HttpServletRequest
    private lateinit var response: HttpServletResponse
    private lateinit var filterChain: FilterChain

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = mockk()
        userDetailsService = mockk()
        jwtAuthenticationFilter = JwtAuthenticationFilter(jwtTokenProvider, userDetailsService)
        request = mockk(relaxed = true)
        response = mockk()
        filterChain = mockk(relaxed = true)
    }

    @Test
    fun `doFilterInternal should set authentication when token is valid`() {
        every { request.getHeader("Authorization") } returns "Bearer valid-token"
        every { request.remoteAddr } returns "127.0.0.1"
        every { jwtTokenProvider.validateToken("valid-token") } returns true
        every { jwtTokenProvider.getUsernameFromToken("valid-token") } returns "testuser"

        val userDetails: UserDetails = User("testuser", "password", listOf())
        every { userDetailsService.loadUserByUsername("testuser") } returns userDetails

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assertNotNull(authentication)
        assert(authentication is UsernamePasswordAuthenticationToken)
        assert((authentication.principal as UserDetails).username == "testuser")
    }

    @Test
    fun `doFilterInternal should not set authentication when token is invalid`() {
        every { request.getHeader("Authorization") } returns "Bearer invalid-token"
        every { jwtTokenProvider.validateToken("invalid-token") } returns false

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assertNull(authentication)
    }

    @Test
    fun `doFilterInternal should handle missing Authorization header`() {
        every { request.getHeader("Authorization") } returns null

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assertNull(authentication)
    }

    @Test
    fun `doFilterInternal should handle blank token`() {
        every { request.getHeader("Authorization") } returns ""

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assertNull(authentication)
    }

    @Test
    fun `doFilterInternal should handle exceptions gracefully`() {
        every { request.getHeader("Authorization") } throws RuntimeException("Unexpected error")

        assertThrows<RuntimeException> {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)
        }
    }
}
