package com.rivertech.bettinggame.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class TransactionHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val initialBalance: Double,
    val betAmount: Double,
    val result: String,
    val winnings: Double,
    val finalBalance: Double,
    val timestamp: LocalDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    @JsonIgnore
    val player: Player
)
