package com.rivertech.bettinggame.service

import com.rivertech.bettinggame.model.*
import com.rivertech.bettinggame.repository.BetRepository
import com.rivertech.bettinggame.repository.PlayerRepository
import com.rivertech.bettinggame.repository.TransactionHistoryRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GameServiceImplTest {

    private lateinit var playerRepository: PlayerRepository
    private lateinit var betRepository: BetRepository
    private lateinit var transactionHistoryRepository: TransactionHistoryRepository
    private lateinit var gameService: GameServiceImpl

    @BeforeEach
    fun setUp() {
        playerRepository = mockk()
        betRepository = mockk()
        transactionHistoryRepository = mockk()
        gameService = GameServiceImpl(playerRepository, betRepository, transactionHistoryRepository)
    }

    @Test
    fun `registerPlayer successfully creates player`() = runTest {
        val player = Player(name = "John", surname = "Doe", username = "johndoe", password = "password")
        every { playerRepository.save(any()) } returns player

        val result = gameService.registerPlayer("John", "Doe", "johndoe", "password")

        assertNotNull(result)
        assertEquals("johndoe", result.username)
        verify { playerRepository.save(any()) }
    }

    @Test
    fun `placeBet places a bet successfully`() = runTest {
        val player = Player(name = "John", surname = "Doe", username = "johndoe", password = "password", wallet = Wallet(balance = 1000.0))
        every { playerRepository.findByUsername("johndoe") } returns player
        every { betRepository.save(any()) } returnsArgument 0
        every { transactionHistoryRepository.save(any()) } returnsArgument 0
        every { playerRepository.save(any()) } returns player

        val bet = gameService.placeBet("johndoe", 100.0, 5)

        assertNotNull(bet)
        assertEquals("johndoe", bet.player.username)
        verify { playerRepository.save(any()) }
        verify { betRepository.save(any()) }
    }

    @Test
    fun `placeBet throws error if balance is insufficient`() = runTest {
        val player = Player(name = "John", surname = "Doe", username = "johndoe", password = "password", wallet = Wallet(balance = 50.0))
        every { playerRepository.findByUsername("johndoe") } returns player

        val exception = assertThrows<IllegalArgumentException> {
            gameService.placeBet("johndoe", 100.0, 5)
        }

        assertEquals("Insufficient balance", exception.message)
    }

    @Test
    fun `login returns player when found`() = runTest {
        val player = Player(name = "John", surname = "Doe", username = "johndoe", password = "password")
        every { playerRepository.findByUsername("johndoe") } returns player

        val result = gameService.login("johndoe", "password")

        assertNotNull(result)
        assertEquals("johndoe", result.username)
    }

    @Test
    fun `getWallet returns wallet successfully`() = runTest {
        val wallet = Wallet(balance = 500.0)
        val player = Player(name = "John", surname = "Doe", username = "johndoe", password = "password", wallet = wallet)
        every { playerRepository.findByUsername("johndoe") } returns player

        val result = gameService.getWallet("johndoe")

        assertNotNull(result)
        assertEquals(500.0, result.balance)
    }

    @Test
    fun `getLeaderboard returns sorted player list`() = runTest {
        val player1 = Player(id = 1L, name = "John", surname = "Doe", username = "johndoe", password = "password")
        val player2 = Player(id = 2L, name = "Jane", surname = "Smith", username = "janesmith", password = "password")
        every { playerRepository.findAll() } returns listOf(player1, player2)
        every { betRepository.findAllByPlayer(any()) } returns listOf(Bet(player = player1, betAmount = 100.0, winnings = 500.0, chosenNumber = 5, generatedNumber = 6, result = "WIN"))

        val leaderboard = gameService.getLeaderboard()

        assertEquals(2, leaderboard.size)
        assertEquals("johndoe", leaderboard[0].username)
    }

    @Test
    fun `getTransactionHistory returns transactions`() = runTest {
        val player = Player(name = "John", surname = "Doe", username = "johndoe", password = "password")
        val transactions = listOf(TransactionHistory(player = player, betAmount = 100.0, result = "WIN", winnings = 500.0, timestamp = LocalDateTime.now(), initialBalance = 1000.0, finalBalance = 1500.0))
        every { playerRepository.findByUsername("johndoe") } returns player
        every { transactionHistoryRepository.findByPlayer(player) } returns transactions

        val result = gameService.getTransactionHistory("johndoe")

        assertEquals(1, result.size)
        assertEquals("WIN", result[0].result)
    }
}
