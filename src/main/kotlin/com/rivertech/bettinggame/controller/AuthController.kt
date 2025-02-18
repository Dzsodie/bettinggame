package com.rivertech.bettinggame.controller

import com.rivertech.bettinggame.service.GameService
import com.rivertech.bettinggame.security.JwtTokenProvider
import com.rivertech.bettinggame.model.Player
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth API", description = "API for Player Registration and Login")
@RestController
@RequestMapping("/auth")
class AuthController(private val gameService: GameService, private val jwtProvider: JwtTokenProvider, private val authenticationManager: AuthenticationManager) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @Operation(summary = "Register a new player", responses = [
        ApiResponse(responseCode = "201", description = "Player registered successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input"),
        ApiResponse(responseCode = "500", description = "Server error")])
    @PostMapping("/register")
    suspend fun registerPlayer(@RequestParam name: String, @RequestParam surname: String, @RequestParam username: String, @RequestParam password: String): ResponseEntity<*> {
        return try {
            val player = gameService.registerPlayer(name, surname, username, password)
            ResponseEntity.status(201).body(player)
        } catch (e: Exception) {
            logger.error("Error registering player: ${'$'}{e.message}", e)
            ResponseEntity.status(500).body("Error registering player")
        }
    }

    @Operation(summary = "Player login", responses = [
        ApiResponse(responseCode = "200", description = "Login successful"),
        ApiResponse(responseCode = "401", description = "Unauthorized"),
        ApiResponse(responseCode = "500", description = "Server error")])
    @PostMapping("/login")
    suspend fun login(@RequestParam username: String, @RequestParam password: String): ResponseEntity<*> {
        return try {
            val authentication: Authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(username, password)
            )

            val player = authentication.principal as Player
            val token = jwtProvider.generateToken(authentication)

            ResponseEntity.ok(mapOf("token" to token, "roles" to player.getAuthorities().map { it.authority }))
        } catch (e: Exception) {
            logger.error("Error during login: ${'$'}{e.message}", e)
            ResponseEntity.status(401).body("Invalid credentials")
        }
    }
}
