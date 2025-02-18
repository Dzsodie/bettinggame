package com.rivertech.bettinggame.service

import com.rivertech.bettinggame.model.Bet
import com.rivertech.bettinggame.model.Player
import com.rivertech.bettinggame.model.TransactionHistory
import com.rivertech.bettinggame.model.Wallet
import com.rivertech.bettinggame.repository.BetRepository
import com.rivertech.bettinggame.repository.PlayerRepository
import com.rivertech.bettinggame.repository.TransactionHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.random.Random

interface GameService {
    suspend fun registerPlayer(name: String, surname: String, username: String, password: String): Player
    suspend fun placeBet(username: String, betAmount: Double, chosenNumber: Int): Bet
    suspend fun login(username: String, password: String): Player
    suspend fun getWallet(username: String): Wallet
    suspend fun getLeaderboard(): List<Player>
    suspend fun getTransactionHistory(username: String): List<TransactionHistory>
}

@Service
class GameServiceImpl(
    private val playerRepository: PlayerRepository,
    private val betRepository: BetRepository,
    private val transactionHistoryRepository: TransactionHistoryRepository
) : GameService {

    private val logger = LoggerFactory.getLogger(GameServiceImpl::class.java)

    override suspend fun registerPlayer(name: String, surname: String, username: String, password: String): Player = withContext(Dispatchers.IO) {
        try {
            val player = Player(name = name, surname = surname, username = username, password = password)
            val wallet = Wallet(balance = 1000.0, player = player)
            player.wallet = wallet

            logger.info("Registering player: $username")
            playerRepository.save(player).also {
                logger.info("Player registered successfully: ${it.username}")
            }
        } catch (e: Exception) {
            logger.error("Error registering player: ${e.message}", e)
            throw e
        }
    }

    override suspend fun placeBet(username: String, betAmount: Double, chosenNumber: Int): Bet = withContext(Dispatchers.IO) {
        try {
            val player = playerRepository.findByUsername(username)
                ?: throw IllegalArgumentException("Player not found")

            val currentBalance = player.wallet?.balance ?: throw IllegalStateException("Player wallet not found")

            require(betAmount <= currentBalance) { "Insufficient balance" }

            val generatedNumber = Random.nextInt(1, 11)
            val winnings = calculateWinnings(betAmount, chosenNumber, generatedNumber)

            val updatedWallet = player.wallet!!.copy(balance = currentBalance - betAmount + winnings)
            player.wallet = updatedWallet

            val bet = Bet(
                player = player,
                betAmount = betAmount,
                chosenNumber = chosenNumber,
                generatedNumber = generatedNumber,
                result = if (winnings > 0) "WIN" else "LOSE",
                winnings = winnings
            )

            val transaction = TransactionHistory(
                initialBalance = currentBalance,
                betAmount = betAmount,
                result = bet.result,
                winnings = winnings,
                finalBalance = player.wallet?.balance!!,
                timestamp = LocalDateTime.now(),
                player = player
            )
            transactionHistoryRepository.save(transaction)

            logger.info("Placing bet for player: $username, Amount: $betAmount, Number: $chosenNumber")
            betRepository.save(bet).also {
                playerRepository.save(player)
                logger.info("Bet placed successfully: Bet ID ${it.id}")
            }
        } catch (e: Exception) {
            logger.error("Error placing bet for player $username: ${e.message}", e)
            throw e
        }
    }

    private fun calculateWinnings(betAmount: Double, chosenNumber: Int, generatedNumber: Int): Double {
        return when (kotlin.math.abs(chosenNumber - generatedNumber)) {
            0 -> betAmount * 10
            1 -> betAmount * 5
            2 -> betAmount / 2
            else -> 0.0
        }
    }
    override suspend fun login(username: String, password: String): Player = withContext(Dispatchers.IO) {
        playerRepository.findByUsername(username) ?: throw IllegalArgumentException("Player not found")
    }

    override suspend fun getWallet(username: String): Wallet = withContext(Dispatchers.IO) {
        playerRepository.findByUsername(username)?.wallet ?: throw IllegalArgumentException("Player not found")
    }

    override suspend fun getLeaderboard(): List<Player> = withContext(Dispatchers.IO) {
        val players = playerRepository.findAll()
        players.sortedByDescending { player ->
            val bets = betRepository.findAllByPlayer(player)
            bets.sumOf { it.winnings }
        }
    }

    override suspend fun getTransactionHistory(username: String): List<TransactionHistory> = withContext(Dispatchers.IO) {
        val player = playerRepository.findByUsername(username) ?: throw IllegalArgumentException("Player not found")
        transactionHistoryRepository.findByPlayer(player) ?: throw IllegalStateException("No transactions found for player")
    }
}
