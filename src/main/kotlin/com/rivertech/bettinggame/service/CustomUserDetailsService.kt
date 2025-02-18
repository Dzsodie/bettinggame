package com.rivertech.bettinggame.service

import com.rivertech.bettinggame.repository.PlayerRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val playerRepository: PlayerRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val player = playerRepository.findByUsername(username) ?: throw UsernameNotFoundException("Player not found")
        val authorities = player.roles.map { SimpleGrantedAuthority(it) }

        return User(player.username, player.password, authorities)
    }
}