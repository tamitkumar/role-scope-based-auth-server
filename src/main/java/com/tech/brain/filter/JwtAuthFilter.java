package com.tech.brain.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final String SECRET_KEY = "MDEyMzQ1Njc4OUFCQ0RFRkdISUpLTE1OT1BRUlNUVVZXWFla";
    private final UserDetailsService userService;
    private final KeyPair keyPair;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("JwtAuthFilter ===> doFilterInternal");
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.info("JwtAuthFilter ===> {}", authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("JwtAuthFilter ===> Invalid token");
            filterChain.doFilter(request, response); // Continue without authentication
            return;
        }

        final String token = authHeader.substring(7);
        final String username = extractUsername(token);
        log.info("JwtAuthFilter ===> username {}", username);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            log.info("JwtAuthFilter ===> username {}", username);
            UserDetails userDetails = userService.loadUserByUsername(username);
            if (isTokenValid(token, userDetails)) {
                log.info("JwtAuthFilter ===> valid token {}", userDetails.getUsername());
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken); // Authenticate
            }
        }
        log.info("AUTH = {}", SecurityContextHolder.getContext().getAuthentication());
        log.info("AUTHORITIES = {}", SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        filterChain.doFilter(request, response);
    }

    private String extractUsername(String token) {
        log.info("JwtAuthFilter ===> extractUsername");
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        log.info("JwtAuthFilter ===> extractAllClaims");
        return Jwts.parser()
                .verifyWith(keyPair.getPublic())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenValid(String token, UserDetails userDetails) {
        log.info("JwtAuthFilter1 ===> Token Valid for {}", userDetails.getUsername());
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        log.info("JwtAuthFilter ===> Token Expired");
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
