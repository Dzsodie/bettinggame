package com.rivertech.bettinggame.controller

import com.rivertech.bettinggame.service.GameService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

fun getSecurityContext(): SecurityContext = SecurityContextHolder.getContext()

@Tag(name = "Game Controller", description = "APIs related to betting, wallet management, and leaderboard.")
@RestController
@RequestMapping("/api")
class GameController(private val gameService: GameService) {

    private val logger = LoggerFactory.getLogger(GameController::class.java)

    @Operation(
        summary = "Place a bet",
        description = "Allows a player to place a bet with a specified amount and chosen number.",
        security = [SecurityRequirement(name = "BearerAuth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Bet placed successfully"),
            ApiResponse(responseCode = "400", description = "Invalid input parameters"),
            ApiResponse(responseCode = "401", description = "Unauthorized - Token missing or invalid"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @PreAuthorize("hasAuthority('ROLE_PLAYER')")
    @PostMapping("/bet")
    suspend fun placeBet(
        @Parameter(description = "Username of the player") @RequestParam username: String,
        @Parameter(description = "Amount to bet") @RequestParam betAmount: Double,
        @Parameter(description = "Number chosen for the bet") @RequestParam chosenNumber: Int
    ): ResponseEntity<*> = withContext(Dispatchers.IO) {
        SecurityContextHolder.setContext(getSecurityContext())
        logger.info("Received bet request for user: $username")
        try {
            val bet = gameService.placeBet(username, betAmount, chosenNumber)
            ResponseEntity.ok(bet)
        } catch (e: Exception) {
            logger.error("Error placing bet", e)
            ResponseEntity.status(500).body("Error placing bet: ${e.message}")
        }
    }

    @Operation(
        summary = "Get wallet balance",
        description = "Retrieves the wallet balance for a given user.",
        security = [SecurityRequirement(name = "BearerAuth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Wallet details retrieved successfully"),
            ApiResponse(responseCode = "401", description = "Unauthorized - Token missing or invalid"),
            ApiResponse(responseCode = "404", description = "Wallet not found"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("/wallet/{username}")
    suspend fun getWallet(
        @Parameter(description = "Username of the player") @PathVariable username: String
    ): ResponseEntity<*> = withContext(Dispatchers.IO) {
        SecurityContextHolder.setContext(getSecurityContext())
        logger.info("Fetching wallet for user: $username")
        try {
            val wallet = gameService.getWallet(username)
            ResponseEntity.ok(wallet)
        } catch (e: Exception) {
            logger.error("Error retrieving wallet", e)
            ResponseEntity.status(500).body("Error retrieving wallet: ${e.message}")
        }
    }

    @Operation(
        summary = "Get leaderboard",
        description = "Fetches the leaderboard ranking based on winnings."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Leaderboard retrieved successfully"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @GetMapping("/leaderboard")
    suspend fun getLeaderboard(): ResponseEntity<*> = withContext(Dispatchers.IO) {
        SecurityContextHolder.setContext(getSecurityContext())
        logger.info("Fetching leaderboard")
        try {
            val leaderboard = gameService.getLeaderboard()
            ResponseEntity.ok(leaderboard)
        } catch (e: Exception) {
            logger.error("Error retrieving leaderboard", e)
            ResponseEntity.status(500).body("Error retrieving leaderboard: ${e.message}")
        }
    }

    @Operation(
        summary = "Get transaction history",
        description = "Retrieves the transaction history for a specific user.",
        security = [SecurityRequirement(name = "BearerAuth")]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully"),
            ApiResponse(responseCode = "401", description = "Unauthorized - Token missing or invalid"),
            ApiResponse(responseCode = "404", description = "User or transactions not found"),
            ApiResponse(responseCode = "500", description = "Internal server error")
        ]
    )
    @PreAuthorize("hasAuthority('ROLE_PLAYER')")
    @GetMapping("/transactions/{username}")
    suspend fun getTransactionHistory(
        @Parameter(description = "Username of the player") @PathVariable username: String
    ): ResponseEntity<*> = withContext(Dispatchers.IO) {
        SecurityContextHolder.setContext(getSecurityContext())
        logger.info("Fetching transactions for user: $username")
        try {
            val transactions = gameService.getTransactionHistory(username)
            ResponseEntity.ok(transactions)
        } catch (e: Exception) {
            logger.error("Error retrieving transactions", e)
            ResponseEntity.status(500).body("Error retrieving transactions: ${e.message}")
        }
    }
}
