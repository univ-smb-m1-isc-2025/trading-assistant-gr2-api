package com.traderalerting.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/star") // Utiliser un nom complètement différent
public class StarController {
    
    private static final Logger log = LoggerFactory.getLogger(StarController.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @PostMapping
    public ResponseEntity<?> addStar(@RequestBody Map<String, String> request,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("Non authentifié");
        }
        
        String ticker = request.get("ticker");
        if (ticker == null || ticker.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Le ticker est requis");
        }
        
        try {
            log.info("Tentative d'ajout du favori {} pour {}", ticker, userDetails.getUsername());
            
            // 1. Obtenir l'ID de l'utilisateur
            Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM app_users WHERE username = ?",
                Long.class,
                userDetails.getUsername()
            );
            
            if (userId == null) {
                return ResponseEntity.status(404).body("Utilisateur non trouvé");
            }
            
            // 2. Vérifier si le symbole existe
            Long symbolId = null;
            try {
                symbolId = jdbcTemplate.queryForObject(
                    "SELECT id FROM symbols WHERE ticker = ?",
                    Long.class,
                    ticker
                );
            } catch (Exception e) {
                // Symbole non trouvé, on l'ajoute
                log.info("Symbole {} non trouvé, ajout...", ticker);
                jdbcTemplate.update(
                    "INSERT INTO symbols (ticker, name) VALUES (?, ?)",
                    ticker, ticker
                );
                
                symbolId = jdbcTemplate.queryForObject(
                    "SELECT id FROM symbols WHERE ticker = ?",
                    Long.class,
                    ticker
                );
            }
            
            // 3. Ajouter la relation utilisateur-symbole
            jdbcTemplate.update(
                "INSERT INTO user_favorites (user_id, symbol_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
                userId, symbolId
            );
            
            return ResponseEntity.ok("Favori ajouté avec succès: " + ticker);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout du favori: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }
}