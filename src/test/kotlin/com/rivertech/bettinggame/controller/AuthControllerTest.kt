package com.rivertech.bettinggame.controller

import com.rivertech.bettinggame.service.GameService
import com.rivertech.bettinggame.model.Player
import com.rivertech.bettinggame.security.JwtTokenProvider
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthControllerTest {

    private lateinit var gameService: GameService
    private lateinit var jwtProvider: JwtTokenProvider
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var authController: AuthController

    @BeforeEach
    fun setUp() {
        gameService = mock(GameService::class.java)
        jwtProvider = mock(JwtTokenProvider::class.java)
        authenticationManager = mock(AuthenticationManager::class.java)
        authController = AuthController(gameService, jwtProvider, authenticationManager)
    }

    @Test
    fun `registerPlayer returns 201 when successful`() = runTest {
        val player = mock(Player::class.java)
        `when`(gameService.registerPlayer("John", "Doe", "johndoe", "password")).thenReturn(player)

        val response = authController.registerPlayer("John", "Doe", "johndoe", "password")

        assertEquals(201, response.statusCodeValue)
        assertEquals(player, response.body)
        verify(gameService).registerPlayer("John", "Doe", "johndoe", "password")
    }

    @Test
    fun `registerPlayer returns 500 on exception`() = runTest {
        `when`(gameService.registerPlayer(anyString(), anyString(), anyString(), anyString())).thenThrow(RuntimeException("Error"))

        val response = authController.registerPlayer("John", "Doe", "johndoe", "password")

        assertEquals(500, response.statusCodeValue)
        assertEquals("Error registering player", response.body)
        verify(gameService).registerPlayer("John", "Doe", "johndoe", "password")
    }

    @Test
    fun `login returns 200 with token when successful`() = runTest {
        val token = "jwt-token"
        val player = mock(Player::class.java)
        val authentication = mock(Authentication::class.java)

        `when`(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken::class.java))).thenReturn(authentication)
        `when`(authentication.principal).thenReturn(player)
        `when`(jwtProvider.generateToken(authentication)).thenReturn(token)

        val response = authController.login("johndoe", "password")

        assertEquals(200, response.statusCodeValue)
        assertNotNull(response.body)
        assertEquals(token, (response.body as Map<*, *>) ["token"])
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken::class.java))
    }

    @Test
    fun `login returns 401 on exception`() = runTest {
        `when`(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken::class.java))).thenThrow(RuntimeException("Invalid credentials"))

        val response = authController.login("johndoe", "password")

        assertEquals(401, response.statusCodeValue)
        assertEquals("Invalid credentials", response.body)
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken::class.java))
    }
}
