package com.traderalerting;

import com.traderalerting.dto.AddFavoriteRequest;
import com.traderalerting.service.FavoriteService;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map; 

@SpringBootApplication
@EnableScheduling
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}

@RestController
@RequestMapping("/api")
class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Trader!";
    }

    @GetMapping("/hello-favorites")
    public String helloFavorites() {
        return "Le service de favoris est disponible!";
    }

    @PostMapping("/favorite-add")
    public ResponseEntity<?> addFavoriteDirect(@RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {

                log.info("Endpoint POST /api/add-favorite appelé");
                log.info("Request body: {}", request);
                log.info("UserDetails présent: {}", (userDetails != null));

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
                    userDetails.getUsername());

            if (userId == null) {
                return ResponseEntity.status(404).body("Utilisateur non trouvé");
            }

            // 2. Vérifier si le symbole existe
            Long symbolId = null;
            try {
                symbolId = jdbcTemplate.queryForObject(
                        "SELECT id FROM symbols WHERE ticker = ?",
                        Long.class,
                        ticker);
            } catch (Exception e) {
                // Symbole non trouvé, on l'ajoute
                log.info("Symbole {} non trouvé, ajout...", ticker);
                jdbcTemplate.update(
                        "INSERT INTO symbols (ticker, name) VALUES (?, ?)",
                        ticker, ticker);

                symbolId = jdbcTemplate.queryForObject(
                        "SELECT id FROM symbols WHERE ticker = ?",
                        Long.class,
                        ticker);
            }

            // 3. Ajouter la relation utilisateur-symbole
            jdbcTemplate.update(
                    "INSERT INTO user_favorites (user_id, symbol_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
                    userId, symbolId);

            return ResponseEntity.ok("Favori ajouté avec succès: " + ticker);

        } catch (Exception e) {
            log.error("Erreur lors de l'ajout du favori: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }
}