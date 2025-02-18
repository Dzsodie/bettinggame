package com.rivertech.bettinggame.repository

import com.rivertech.bettinggame.model.Bet
import com.rivertech.bettinggame.model.Player
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BetRepository : JpaRepository<Bet, Long> {
    fun findAllByPlayer(player: Player): List<Bet>
}