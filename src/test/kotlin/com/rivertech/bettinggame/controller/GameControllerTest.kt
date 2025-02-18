package com.rivertech.bettinggame.controller

import com.rivertech.bettinggame.model.Bet
import com.rivertech.bettinggame.model.Wallet
import com.rivertech.bettinggame.model.Player
import com.rivertech.bettinggame.model.TransactionHistory
import com.rivertech.bettinggame.service.GameService
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameControllerTest {

    private lateinit var gameService: GameService
    private lateinit var gameController: GameController

    @BeforeEach
    fun setUp() {
        gameService = mockk()
        gameController = GameController(gameService)
    }

    @Test
    fun `placeBet returns bet successfully`() = runTest {
        val player = Player(id = 1L, name = "John", surname = "Doe", username = "johndoe", password = "password")
        val bet = Bet(1L, player, 100.0, 5, 5,"WIN", 1000.0)
        coEvery { gameService.placeBet("johndoe", 100.0, 5) } returns bet

        val response = gameController.placeBet("johndoe", 100.0, 5)

        assertEquals(200, response.statusCodeValue)
        assertEquals(bet, response.body)
        coVerify { gameService.placeBet("johndoe", 100.0, 5) }
    }

    @Test
    fun `getWallet returns wallet successfully`() = runTest {
        val player = Player(id = 1L, name = "John", surname = "Doe", username = "johndoe", password = "password")
        val wallet = Wallet(1L, 500.0, player)
        coEvery { gameService.getWallet("johndoe") } returns wallet

        val response = gameController.getWallet("johndoe")

        assertEquals(200, response.statusCodeValue)
        assertEquals(wallet, response.body)
    }

    @Test
    fun `getLeaderboard returns leaderboard successfully`() = runTest {
        val player1 = Player(id = 1L, name = "John", surname = "Doe", username = "johndoe", password = "password")
        val player2 = Player(id = 2L, name = "Jane", surname = "Smith", username = "janesmith", password = "password")
        val leaderboard = listOf(player1, player2)
        coEvery { gameService.getLeaderboard() } returns leaderboard

        val response = gameController.getLeaderboard()

        assertEquals(200, response.statusCodeValue)
        assertEquals(leaderboard, response.body)
    }

    @Test
    fun `getTransactionHistory returns transactions successfully`() = runTest {
        val player = Player(id = 1L, name = "John", surname = "Doe", username = "johndoe", password = "password")
        val transaction1 = TransactionHistory(1L, 1000.0, 100.0, "WIN", 1000.0, 2000.0, LocalDateTime.now(), player )
        val transaction2 = TransactionHistory(2L, 2000.0, 50.0, "LOSS", 0.0, 1950.0, LocalDateTime.now().plusMinutes(10), player)
        val transactions = listOf(transaction1, transaction2)
        coEvery { gameService.getTransactionHistory("johndoe") } returns transactions

        val response = gameController.getTransactionHistory("johndoe")

        assertEquals(200, response.statusCodeValue)
        assertEquals(transactions, response.body)
    }

    @Test
    fun `placeBet returns error when exception occurs`() = runTest {
        coEvery { gameService.placeBet(any(), any(), any()) } throws RuntimeException("Bet failed")

        val response = gameController.placeBet("johndoe", 100.0, 5)

        assertEquals(500, response.statusCodeValue)
        assertTrue((response.body as String).contains("Error placing bet"))
    }

    @Test
    fun `getWallet returns error when exception occurs`() = runTest {
        coEvery { gameService.getWallet(any()) } throws RuntimeException("Wallet error")

        val response = gameController.getWallet("johndoe")

        assertEquals(500, response.statusCodeValue)
        assertTrue((response.body as String).contains("Error retrieving wallet"))
    }

    @Test
    fun `getLeaderboard returns error when exception occurs`() = runTest {
        coEvery { gameService.getLeaderboard() } throws RuntimeException("Leaderboard error")

        val response = gameController.getLeaderboard()

        assertEquals(500, response.statusCodeValue)
        assertTrue((response.body as String).contains("Error retrieving leaderboard"))
    }

    @Test
    fun `getTransactionHistory returns error when exception occurs`() = runTest {
        coEvery { gameService.getTransactionHistory(any()) } throws RuntimeException("Transaction error")

        val response = gameController.getTransactionHistory("johndoe")

        assertEquals(500, response.statusCodeValue)
        assertTrue((response.body as String).contains("Error retrieving transactions"))
    }
}
