package com.rivertech.bettinggame.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.rivertech.bettinggame.model.Player

@Repository
interface PlayerRepository : JpaRepository<Player, Long> {
    fun findByUsername(username: String): Player?
    fun existsByUsername(username: String): Boolean
}