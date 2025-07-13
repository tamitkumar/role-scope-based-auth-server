package com.tech.brain.config;

import com.tech.brain.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("DialectConfig ===> http {}", http.toString());
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/token",
                                "/test/welcome",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/api/auth/.well-known/jwks.json",
                                "/webjars/**").permitAll()
                        .requestMatchers("/register/new/service")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_CLIENT", "ROLE_SERVICE", "ROLE_HR", "ROLE_USER")
                        .requestMatchers("/register/service/all")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/register/approve/{scopeId}")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/register/new/role")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/register/scope/approve")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/register/scope/approve")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/register/scope/request/status")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_CLIENT", "ROLE_SERVICE", "ROLE_HR", "ROLE_USER")
                        .requestMatchers("/register/scope/list/{serviceId}")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/register/scope/approve/{serviceId}")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")

                        .requestMatchers("/register/scope/promote/{userId}")
                        .hasAnyAuthority("ROLE_ADMIN")
                        .requestMatchers("/register/scope/demote/{userId}")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/register/scope/users-with-roles")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_CLIENT", "ROLE_SERVICE", "ROLE_HR")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable).build();
    }
}


