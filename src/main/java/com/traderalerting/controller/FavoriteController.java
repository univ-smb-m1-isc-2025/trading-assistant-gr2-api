package com.traderalerting.controller;

import com.traderalerting.dto.AddFavoriteRequest;
import com.traderalerting.dto.SymbolDto;
import com.traderalerting.service.FavoriteService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.Set;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    
    private static final Logger log = LoggerFactory.getLogger(FavoriteController.class);
    
    @Autowired
    private FavoriteService favoriteService;
    
    @PostConstruct
    public void init() {
        log.info("FavoriteController initialisé avec path /api/favorites");
    }
    
    // Endpoint de test public pour vérifier l'accessibilité du contrôleur
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        log.info("Endpoint de test appelé");
        return ResponseEntity.ok("Le contrôleur FavoriteController fonctionne correctement!");
    }
    
    // Récupérer les favoris de l'utilisateur connecté
    @GetMapping
    public ResponseEntity<Set<SymbolDto>> getUserFavorites(@AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            log.warn("Tentative d'accès aux favoris sans authentification");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        log.info("Récupération des favoris pour: {}", currentUser.getUsername());
        Set<SymbolDto> favorites = favoriteService.getFavorites(currentUser.getUsername());
        return ResponseEntity.ok(favorites);
    }
    
    // Ajouter un favori
    @PostMapping
    public ResponseEntity<?> addFavorite(@AuthenticationPrincipal UserDetails currentUser, 
                                         @Valid @RequestBody AddFavoriteRequest request) {
        if (currentUser == null) {
            log.warn("Tentative d'ajout de favori sans authentification");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            log.info("Ajout du favori {} pour {}", request.getTicker(), currentUser.getUsername());
            favoriteService.addFavorite(currentUser.getUsername(), request.getTicker());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout du favori: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Supprimer un favori
    @DeleteMapping("/{ticker}")
    public ResponseEntity<?> removeFavorite(@AuthenticationPrincipal UserDetails currentUser,
                                            @PathVariable String ticker) {
        if (currentUser == null) {
            log.warn("Tentative de suppression de favori sans authentification");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            log.info("Suppression du favori {} pour {}", ticker, currentUser.getUsername());
            favoriteService.removeFavorite(currentUser.getUsername(), ticker);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du favori: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}