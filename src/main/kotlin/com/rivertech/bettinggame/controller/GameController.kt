package com.rivertech.bettinggame.controller

import com.rivertech.bettinggame.service.GameService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory

fun getSecurityContext(): SecurityContext = SecurityContextHolder.getContext()

@RestController
@RequestMapping("/api")
class GameController(private val gameService: GameService) {

    private val logger = LoggerFactory.getLogger(GameController::class.java)

    @PreAuthorize("hasAuthority('ROLE_PLAYER')")
    @PostMapping("/bet")
    suspend fun placeBet(@RequestParam username: String, @RequestParam betAmount: Double, @RequestParam chosenNumber: Int): ResponseEntity<*> = withContext(Dispatchers.IO) {
        SecurityContextHolder.setContext(getSecurityContext())
        logger.info("Received bet request for user: $username")
        try {
            val bet = gameService.placeBet(username, betAmount, chosenNumber)
            ResponseEntity.ok(bet)
        } catch (e: Exception) {
            logger.error("Error placing bet", e)
            ResponseEntity.status(500).body("Error placing bet: ${'$'}{e.message}")
        }
    }

    @GetMapping("/wallet/{username}")
    suspend fun getWallet(@PathVariable username: String): ResponseEntity<*> = withContext(Dispatchers.IO) {
        SecurityContextHolder.setContext(getSecurityContext())
        logger.info("Fetching wallet for user: $username")
        try {
            val wallet = gameService.getWallet(username)
            ResponseEntity.ok(wallet)
        } catch (e: Exception) {
            logger.error("Error retrieving wallet", e)
            ResponseEntity.status(500).body("Error retrieving wallet: ${'$'}{e.message}")
        }
    }

    @GetMapping("/leaderboard")
    suspend fun getLeaderboard(): ResponseEntity<*> = withContext(Dispatchers.IO) {
        SecurityContextHolder.setContext(getSecurityContext())
        logger.info("Fetching leaderboard")
        try {
            val leaderboard = gameService.getLeaderboard()
            ResponseEntity.ok(leaderboard)
        } catch (e: Exception) {
            logger.error("Error retrieving leaderboard", e)
            ResponseEntity.status(500).body("Error retrieving leaderboard: ${'$'}{e.message}")
        }
    }

    @PreAuthorize("hasAuthority('ROLE_PLAYER')")
    @GetMapping("/transactions/{username}")
    suspend fun getTransactionHistory(@PathVariable username: String): ResponseEntity<*> = withContext(Dispatchers.IO) {
        SecurityContextHolder.setContext(getSecurityContext())
        logger.info("Fetching transactions for user: $username")
        try {
            val transactions = gameService.getTransactionHistory(username)
            ResponseEntity.ok(transactions)
        } catch (e: Exception) {
            logger.error("Error retrieving transactions", e)
            ResponseEntity.status(500).body("Error retrieving transactions: ${'$'}{e.message}")
        }
    }
}
