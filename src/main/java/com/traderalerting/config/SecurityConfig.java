package com.traderalerting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Désactiver CSRF pour les tests
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Autoriser toutes les requêtes sans authentification
                );
        return http.build();
    }
}
