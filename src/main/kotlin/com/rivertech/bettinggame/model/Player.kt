package com.rivertech.bettinggame.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

@Entity
@Table(name = "players")
data class Player(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val name: String,
    val surname: String,
    val username: String,
    val password: String,

    @OneToOne(mappedBy = "player", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    var wallet: Wallet? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    val roles: Set<String> = setOf("ROLE_PLAYER")
)  {
    fun getAuthorities(): List<GrantedAuthority> = roles.map { SimpleGrantedAuthority(it) }
}