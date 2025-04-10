package com.traderalerting.registration.controller;

import com.traderalerting.dto.LoginRequest;
import com.traderalerting.dto.AuthResponse;
import com.traderalerting.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // Importez l'interface
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api") 
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService; // Utilisez l'interface

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Tente l'authentification via Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Si l'authentification réussit (aucune exception lancée)
            // On récupère les UserDetails pour générer le token
            // Note : Le principal de l'objet Authentication EST l'UserDetails
            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Alternative : recharger explicitement si nécessaire (normalement pas utile ici)
            // final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsernameOrEmail());

            final String jwt = jwtService.generateToken(userDetails);

            // Retourne le token JWT dans la réponse
            return ResponseEntity.ok(new AuthResponse(jwt));

        } catch (BadCredentialsException e) {
            // Si l'authentification échoue (mauvais identifiants)
            return ResponseEntity.status(401).body("Invalid username/email or password");
        } catch (Exception e) {
            // Autres erreurs potentielles
             e.printStackTrace(); // Loggez l'erreur pour le debug
             return ResponseEntity.status(500).body("An error occurred during authentication");
        }
    }
}