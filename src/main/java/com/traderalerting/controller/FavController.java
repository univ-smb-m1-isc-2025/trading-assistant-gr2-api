package com.traderalerting.controller;

import com.traderalerting.dto.AddFavoriteRequest;
import com.traderalerting.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fav") // Utiliser un chemin différent
public class FavController {
    
    private static final Logger log = LoggerFactory.getLogger(FavController.class);
    
    @Autowired
    private FavoriteService favoriteService;
    
    // Endpoint de test public pour vérifier l'accessibilité
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        log.info("Endpoint de test appelé");
        return ResponseEntity.ok("Le contrôleur des favoris fonctionne correctement!");
    }
    
    // Ajouter un favori
    @PostMapping
    public ResponseEntity<?> addFavorite(@AuthenticationPrincipal UserDetails currentUser,
                                         @RequestBody AddFavoriteRequest request) {
        if (currentUser == null) {
            log.warn("Tentative d'ajout de favori sans authentification");
            return ResponseEntity.status(401).body("Non authentifié");
        }
        
        try {
            log.info("Ajout du favori {} pour {}", request.getTicker(), currentUser.getUsername());
            favoriteService.addFavorite(currentUser.getUsername(), request.getTicker());
            return ResponseEntity.ok().body("Favori ajouté avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout du favori: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}