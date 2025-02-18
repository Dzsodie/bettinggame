package com.rivertech.bettinggame.config

import com.rivertech.bettinggame.security.JwtTokenProvider
import com.rivertech.bettinggame.service.CustomUserDetailsService
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.DefaultSecurityFilterChain
import kotlin.test.assertNotNull

class SecurityConfigTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var userDetailsService: CustomUserDetailsService
    private lateinit var securityConfig: SecurityConfig
    private lateinit var logger: Logger

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = mockk()
        userDetailsService = mockk()
        securityConfig = SecurityConfig(jwtTokenProvider, userDetailsService)
        logger = LoggerFactory.getLogger(SecurityConfig::class.java)
    }

    @Test
    fun `passwordEncoder should return a BCryptPasswordEncoder`() {
        val encoder = securityConfig.passwordEncoder()
        assertNotNull(encoder)
        assert(encoder is BCryptPasswordEncoder)
    }

    @Test
    fun `authenticationManager should configure properly`() {
        val passwordEncoder: PasswordEncoder = mockk()
        every { passwordEncoder.encode(any()) } returns "encodedPassword"
        every { userDetailsService.loadUserByUsername(any()) } returns mockk()

        val authenticationManager = securityConfig.authenticationManager(passwordEncoder)

        assertNotNull(authenticationManager)
        assert(authenticationManager is ProviderManager)
    }

    @Test
    fun `securityFilterChain should configure properly`() {
        val httpSecurity: HttpSecurity = mockk(relaxed = true)
        every { httpSecurity.csrf(any()) } returns httpSecurity
        every { httpSecurity.authorizeHttpRequests(any()) } returns httpSecurity
        every { httpSecurity.addFilterBefore(any(), any()) } returns httpSecurity
        every { httpSecurity.sessionManagement(any()) } returns httpSecurity
        every { httpSecurity.build() } returns mockk<DefaultSecurityFilterChain>()
        val filterChain = securityConfig.securityFilterChain(httpSecurity)

        assertNotNull(filterChain)
    }
}
