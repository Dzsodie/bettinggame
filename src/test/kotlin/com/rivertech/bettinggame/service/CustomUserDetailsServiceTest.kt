package com.rivertech.bettinggame.service

import com.rivertech.bettinggame.model.Player
import com.rivertech.bettinggame.repository.PlayerRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.userdetails.UsernameNotFoundException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CustomUserDetailsServiceTest {

    private lateinit var playerRepository: PlayerRepository
    private lateinit var customUserDetailsService: CustomUserDetailsService

    @BeforeEach
    fun setUp() {
        playerRepository = mockk()
        customUserDetailsService = CustomUserDetailsService(playerRepository)
    }

    @Test
    fun `loadUserByUsername returns UserDetails successfully`() {
        val player = Player(
            id = 1L,
            name = "John",
            surname = "Doe",
            username = "johndoe",
            password = "password",
            roles = setOf("ROLE_PLAYER")
        )

        every { playerRepository.findByUsername("johndoe") } returns player

        val userDetails = customUserDetailsService.loadUserByUsername("johndoe")

        assertNotNull(userDetails)
        assertEquals("johndoe", userDetails.username)
        assertEquals("password", userDetails.password)
        assertEquals(1, userDetails.authorities.size)
        assertEquals("ROLE_PLAYER", userDetails.authorities.first().authority)

        verify { playerRepository.findByUsername("johndoe") }
    }

    @Test
    fun `loadUserByUsername throws exception when user not found`() {
        every { playerRepository.findByUsername("unknown") } returns null

        val exception = assertThrows<UsernameNotFoundException> {
            customUserDetailsService.loadUserByUsername("unknown")
        }

        assertEquals("Player not found", exception.message)
        verify { playerRepository.findByUsername("unknown") }
    }
}
