package com.rivertech.bettinggame.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    val jwtTokenProvider: JwtTokenProvider,
    val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    public override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val token = getJwtFromRequest(request)
        logger.info("Extracted Token: $token")

        if (!token.isNullOrBlank() && jwtTokenProvider.validateToken(token)) {
            val username = jwtTokenProvider.getUsernameFromToken(token)
            val userDetails: UserDetails = userDetailsService.loadUserByUsername(username)

            val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
            authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authentication
        } else {
            logger.warn("Invalid or missing JWT token")
        }

        filterChain.doFilter(request, response)
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (!bearerToken.isNullOrBlank() && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
}