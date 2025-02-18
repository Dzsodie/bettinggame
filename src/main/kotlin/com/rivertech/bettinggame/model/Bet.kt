package com.rivertech.bettinggame.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
data class Bet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    @JsonIgnore
    val player: Player,

    val betAmount: Double,
    val chosenNumber: Int,
    val generatedNumber: Int,
    val result: String,
    val winnings: Double
)