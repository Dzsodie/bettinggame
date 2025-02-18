package com.rivertech.bettinggame.repository

import com.rivertech.bettinggame.model.Player
import com.rivertech.bettinggame.model.TransactionHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TransactionHistoryRepository : JpaRepository<TransactionHistory, Long> {
    fun findByPlayer(player: Player): List<TransactionHistory>?
}