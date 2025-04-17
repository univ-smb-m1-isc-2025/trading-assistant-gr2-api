package com.traderalerting.init;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Initialisation de la base de données...");
        
        try {
            // Vérifier la connexion à la base de données
            String dbProductName = jdbcTemplate.queryForObject("SELECT version()", String.class);
            log.info("Connecté à la base de données: {}", dbProductName);
            
            // Vérifier les tables existantes
            log.info("Tables existantes:");
            jdbcTemplate.queryForList("SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'")
                .forEach(row -> log.info(" - {}", row.get("table_name")));
            
            // Créer manuellement les tables si nécessaire
            if (!tableExists("symbols")) {
                log.info("Création de la table symbols...");
                jdbcTemplate.execute(
                    "CREATE TABLE symbols (" +
                    "  id SERIAL PRIMARY KEY," +
                    "  ticker VARCHAR(20) NOT NULL UNIQUE," +
                    "  name VARCHAR(100) NOT NULL" +
                    ")"
                );
            }
            
            if (!tableExists("user_favorites")) {
                log.info("Création de la table user_favorites...");
                jdbcTemplate.execute(
                    "CREATE TABLE user_favorites (" +
                    "  user_id BIGINT NOT NULL," +
                    "  symbol_id BIGINT NOT NULL," +
                    "  PRIMARY KEY (user_id, symbol_id)," +
                    "  FOREIGN KEY (user_id) REFERENCES app_users(id)," +
                    "  FOREIGN KEY (symbol_id) REFERENCES symbols(id)" +
                    ")"
                );
            }
            
            // Insérer quelques symboles de base
            if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM symbols", Integer.class) == 0) {
                log.info("Insertion des symboles de base...");
                String[] symbols = {
                    "AIR.PA,Airbus",
                    "AI.PA,Air Liquide",
                    "MC.PA,LVMH"
                };
                
                for (String symbolData : symbols) {
                    String[] parts = symbolData.split(",");
                    jdbcTemplate.update(
                        "INSERT INTO symbols (ticker, name) VALUES (?, ?) ON CONFLICT DO NOTHING",
                        parts[0], parts[1]
                    );
                }
            }
            
            log.info("Initialisation de la base de données terminée avec succès.");
        } catch (Exception e) {
            log.error("Erreur lors de l'initialisation de la base de données: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?",
            Integer.class,
            tableName
        );
        return count != null && count > 0;
    }
}