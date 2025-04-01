package com.traderalerting.config; // Assurez-vous que le package est correct

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <-- Importer HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // Souvent utile
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults; // <-- Importer withDefaults

@Configuration
@EnableWebSecurity // Active la sécurité web Spring
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
         http
            .cors(withDefaults()) // <-- **IMPORTANT : Intègre la configuration CORS**
            .csrf(csrf -> csrf.disable()) // Désactive CSRF si API stateless (JWT etc.)
            .authorizeHttpRequests(authz -> authz
                    // --- Permettre les requêtes OPTIONS (pour le preflight CORS) ---
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Autorise TOUTES les OPTIONS
                    // --- Vos règles spécifiques ---
                    .requestMatchers("/api/register", "/error").permitAll()
                    // --- Règle finale ---
                    .anyRequest().permitAll() // Pour le moment, autorise tout le reste
                   // ou .anyRequest().authenticated() // Si vous voulez sécuriser le reste plus tard
            );
            // .httpBasic(withDefaults()); // Décommentez si vous utilisez l'authentification HTTP Basic

        return http.build();
    }
}