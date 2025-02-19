package com.rivertech.bettinggame.config

import com.rivertech.bettinggame.security.JwtAuthenticationFilter
import com.rivertech.bettinggame.security.JwtTokenProvider
import com.rivertech.bettinggame.service.CustomUserDetailsService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: CustomUserDetailsService
) {

    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    init {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        logger.info("Initializing password encoder")
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(passwordEncoder: PasswordEncoder): AuthenticationManager {
        logger.info("Configuring authentication manager")
        return try {
            val authProvider = DaoAuthenticationProvider().apply {
                setUserDetailsService(userDetailsService)
                setPasswordEncoder(passwordEncoder)
            }
            ProviderManager(authProvider).also {
                logger.info("Authentication manager configured successfully")
            }
        } catch (e: Exception) {
            logger.error("Error configuring authentication manager: ", e)
            throw e
        }
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        logger.info("Configuring security filter chain")
        return try {
            http
                .csrf { it.disable() }
                .authorizeHttpRequests { auth ->
                    auth.requestMatchers(
                        "/auth/register", "/auth/login",
                        "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**"
                    ).permitAll()
                        .requestMatchers("/api/bet").hasAuthority("ROLE_PLAYER")
                        .requestMatchers("/api/wallet/**").hasAuthority("ROLE_PLAYER")
                        .requestMatchers("/api/leaderboard").hasAnyRole("PLAYER", "ADMIN")
                        .requestMatchers("/api/transactions/**").hasAuthority("ROLE_PLAYER")
                        .anyRequest().permitAll()
                }
                .addFilterBefore(JwtAuthenticationFilter(jwtTokenProvider, userDetailsService), UsernamePasswordAuthenticationFilter::class.java)
                .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

            logger.info("Security filter chain configured successfully")
            http.build()
        } catch (e: Exception) {
            logger.error("Error configuring security filter chain: ", e)
            throw e
        }
    }
}
