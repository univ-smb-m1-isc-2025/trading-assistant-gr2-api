package com.traderalerting.scheduler;

import com.traderalerting.service.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CAC40AlertScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CAC40AlertScheduler.class);
    
    // Liste des actions du CAC40
    private static final List<String> CAC40_SYMBOLS = Arrays.asList(
            "AC.PA", "ACA.PA", "AI.PA", "AIR.PA", "ALO.PA", "BN.PA", "BNP.PA", "CA.PA", 
            "CAP.PA", "CS.PA", "DG.PA", "DSY.PA", "EN.PA", "ENGI.PA", "GLE.PA", "HO.PA", 
            "KER.PA", "LR.PA", "MC.PA", "ML.PA", "OR.PA", "ORA.PA", "PUB.PA", "RI.PA", 
            "RMS.PA", "RNO.PA", "SAF.PA", "SAN.PA", "SGO.PA", "STLAP.PA", "STM.PA", "SU.PA", 
            "SW.PA", "TTE.PA", "URW.AS", "VIE.PA", "VIV.PA", "WLN.PA");
    
    // Map des symboles Yahoo Finance vers noms réels des sociétés
    private static final Map<String, String> COMPANY_NAMES = new HashMap<>();
    static {
        COMPANY_NAMES.put("AC.PA", "Accor");
        COMPANY_NAMES.put("ACA.PA", "Crédit Agricole");
        COMPANY_NAMES.put("AI.PA", "Air Liquide");
        COMPANY_NAMES.put("AIR.PA", "Airbus");
        COMPANY_NAMES.put("ALO.PA", "Alstom");
        COMPANY_NAMES.put("BN.PA", "Danone");
        COMPANY_NAMES.put("BNP.PA", "BNP Paribas");
        COMPANY_NAMES.put("CA.PA", "Carrefour");
        COMPANY_NAMES.put("CAP.PA", "Capgemini");
        COMPANY_NAMES.put("CS.PA", "AXA");
        COMPANY_NAMES.put("DG.PA", "Vinci");
        COMPANY_NAMES.put("DSY.PA", "Dassault Systèmes");
        COMPANY_NAMES.put("EN.PA", "Bouygues");
        COMPANY_NAMES.put("ENGI.PA", "Engie");
        COMPANY_NAMES.put("GLE.PA", "Société Générale");
        COMPANY_NAMES.put("HO.PA", "Thales");
        COMPANY_NAMES.put("KER.PA", "Kering");
        COMPANY_NAMES.put("LR.PA", "Legrand");
        COMPANY_NAMES.put("MC.PA", "LVMH");
        COMPANY_NAMES.put("ML.PA", "Michelin");
        COMPANY_NAMES.put("OR.PA", "L'Oréal");
        COMPANY_NAMES.put("ORA.PA", "Orange");
        COMPANY_NAMES.put("PUB.PA", "Publicis");
        COMPANY_NAMES.put("RI.PA", "Pernod Ricard");
        COMPANY_NAMES.put("RMS.PA", "Hermès");
        COMPANY_NAMES.put("RNO.PA", "Renault");
        COMPANY_NAMES.put("SAF.PA", "Safran");
        COMPANY_NAMES.put("SAN.PA", "Sanofi");
        COMPANY_NAMES.put("SGO.PA", "Saint-Gobain");
        COMPANY_NAMES.put("STLAP.PA", "ArcelorMittal");
        COMPANY_NAMES.put("STM.PA", "STMicroelectronics");
        COMPANY_NAMES.put("SU.PA", "Schneider Electric");
        COMPANY_NAMES.put("SW.PA", "Sodexo");
        COMPANY_NAMES.put("TTE.PA", "TotalEnergies");
        COMPANY_NAMES.put("URW.AS", "Unibail-Rodamco-Westfield");
        COMPANY_NAMES.put("VIE.PA", "Veolia");
        COMPANY_NAMES.put("VIV.PA", "Vivendi");
        COMPANY_NAMES.put("WLN.PA", "Worldline");
    }
    
    // Map des types d'alertes vers leurs ID
    private static final Map<String, Integer> ALERT_TYPE_IDS = new HashMap<>();
    static {
        ALERT_TYPE_IDS.put("priceVariation", 1);
        ALERT_TYPE_IDS.put("candlePattern", 2);
        ALERT_TYPE_IDS.put("movingAverageCrossover", 3);
        ALERT_TYPE_IDS.put("stockIncrease", 4);
        ALERT_TYPE_IDS.put("thresholdAlert", 5);
    }
    
    @Autowired
    private PriceAlertService priceAlertService;
    
    @Autowired
    private CandlePatternService candlePatternService;
    
    @Autowired
    private MovingAverageCrossoverService movingAverageCrossoverService;
    
    @Autowired
    private StockAlertService stockAlertService;
    
    @Autowired
    private AlertThresholdService alertThresholdService;
    
    @Autowired
    private EmailService emailService;
    
    // Email de l'utilisateur pour recevoir les alertes
    @Value("charriersim@gmail.com")
    private String alertsRecipientEmail;
    
    // Paramètres par défaut pour les alertes
    private static final double PRICE_VARIATION_THRESHOLD = 7.5;
    private static final int PRICE_VARIATION_DAYS = 5;
    private static final double STOCK_INCREASE_THRESHOLD = 3.0;
    private static final double ALERT_THRESHOLD = 2.0;
    private static final int SHORT_MA = 10;
    private static final int MEDIUM_MA = 50;
    private static final int LONG_MA = 200;
    private static final String RANGE = "1mo";
    
    // Exécution tous les jours à 18:30 (après la fermeture du marché CAC40 à 17:30 + 1h de marge)
    @Scheduled(cron = "0 30 18 * * MON-FRI")
    public void scanCAC40Stocks() {
        logger.info("Démarrage de l'analyse des actions du CAC40 - {}", 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        Map<String, Map<String, Object>> alertResults = new HashMap<>();
        
        for (String symbol : CAC40_SYMBOLS) {
            Map<String, Object> stockAlerts = new HashMap<>();
            logger.info("Analyse de l'action: {}", symbol);
            
            try {
                // 1. Vérification des variations de prix
                checkPriceVariation(symbol, stockAlerts);
                
                // 2. Vérification des motifs de chandelier (Dragonfly Doji)
                checkCandlePatterns(symbol, stockAlerts);
                
                // 3. Vérification des croisements de moyennes mobiles
                checkMovingAverageCrossover(symbol, stockAlerts);
                
                // 4. Vérification des augmentations de prix
                checkStockIncrease(symbol, stockAlerts);
                
                // 5. Vérification des seuils d'alerte
                checkAlertThreshold(symbol, stockAlerts);
                
                // Si au moins une alerte est déclenchée, ajouter aux résultats
                if (stockAlerts.values().stream().anyMatch(v -> (boolean)((Map<String, Object>)v).get("triggered"))) {
                    alertResults.put(symbol, stockAlerts);
                }
                
            } catch (Exception e) {
                logger.error("Erreur lors de l'analyse de {}: {}", symbol, e.getMessage());
            }
        }
        
        // Enregistrer et notifier les résultats
        if (!alertResults.isEmpty()) {
            logAlertResults(alertResults);
            sendEmailNotifications(alertResults);
        } else {
            logger.info("Aucune alerte déclenchée aujourd'hui.");
        }
        
        logger.info("Analyse des actions du CAC40 terminée - {}", 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    /**
     * Envoie des notifications par email pour chaque alerte déclenchée
     */
    private void sendEmailNotifications(Map<String, Map<String, Object>> alertResults) {
        logger.info("Envoi des notifications par email pour {} actions", alertResults.size());
        
        for (Map.Entry<String, Map<String, Object>> entry : alertResults.entrySet()) {
            String symbol = entry.getKey();
            Map<String, Object> alerts = entry.getValue();
            String companyName = COMPANY_NAMES.getOrDefault(symbol, symbol);
            
            for (Map.Entry<String, Object> alertEntry : alerts.entrySet()) {
                String alertType = alertEntry.getKey();
                Map<String, Object> alertInfo = (Map<String, Object>) alertEntry.getValue();
                
                boolean triggered = (boolean) alertInfo.get("triggered");
                
                if (triggered) {
                    // Obtenir l'ID de l'alerte
                    int alerteId = ALERT_TYPE_IDS.getOrDefault(alertType, 0);
                    
                    try {
                        // Envoyer l'email d'alerte
                        emailService.sendAlerteMail(alertsRecipientEmail, companyName, alerteId);
                        logger.info("Email d'alerte envoyé pour {} - Type d'alerte: {}", companyName, alertType);
                    } catch (Exception e) {
                        logger.error("Erreur lors de l'envoi de l'email pour {}: {}", companyName, e.getMessage());
                    }
                }
            }
        }
    }
    
    private void checkPriceVariation(String symbol, Map<String, Object> stockAlerts) {
        try {
            PriceAlertService.PriceVariationResult result = 
                    priceAlertService.checkPriceVariation(symbol, PRICE_VARIATION_DAYS, PRICE_VARIATION_THRESHOLD);
            
            Map<String, Object> alertInfo = new HashMap<>();
            alertInfo.put("triggered", result.isExceedsThreshold());
            alertInfo.put("variationPercent", result.getVariationPercent());
            alertInfo.put("minPrice", result.getMinPrice());
            alertInfo.put("maxPrice", result.getMaxPrice());
            alertInfo.put("currentPrice", result.getCurrentPrice());
            alertInfo.put("message", result.getMessage());
            
            stockAlerts.put("priceVariation", alertInfo);
            
        } catch (IOException e) {
            logger.error("Erreur lors de la vérification de la variation de prix pour {}: {}", symbol, e.getMessage());
            Map<String, Object> alertInfo = new HashMap<>();
            alertInfo.put("triggered", false);
            alertInfo.put("error", e.getMessage());
            stockAlerts.put("priceVariation", alertInfo);
        }
    }
    
    private void checkCandlePatterns(String symbol, Map<String, Object> stockAlerts) {
        try {
            List<String> dojiDates = candlePatternService.detectDragonflyDoji(symbol, RANGE);
            
            // Vérifier si un Dragonfly Doji a été détecté aujourd'hui
            boolean hasTodayDoji = dojiDates.contains(LocalDate.now().toString());
            
            Map<String, Object> alertInfo = new HashMap<>();
            alertInfo.put("triggered", hasTodayDoji);
            alertInfo.put("pattern", "Dragonfly Doji");
            alertInfo.put("detectedDates", dojiDates);
            
            stockAlerts.put("candlePattern", alertInfo);
            
        } catch (IOException e) {
            logger.error("Erreur lors de la vérification des motifs de chandelier pour {}: {}", symbol, e.getMessage());
            Map<String, Object> alertInfo = new HashMap<>();
            alertInfo.put("triggered", false);
            alertInfo.put("error", e.getMessage());
            stockAlerts.put("candlePattern", alertInfo);
        }
    }
    
    private void checkMovingAverageCrossover(String symbol, Map<String, Object> stockAlerts) {
        try {
            JSONObject result = movingAverageCrossoverService.detectCrossover(
                    symbol, SHORT_MA, MEDIUM_MA, LONG_MA, RANGE);
            
            boolean goldenCross = result.has("goldenCross") && result.getBoolean("goldenCross");
            boolean deathCross = result.has("deathCross") && result.getBoolean("deathCross");
            
            Map<String, Object> alertInfo = new HashMap<>();
            alertInfo.put("triggered", goldenCross || deathCross);
            alertInfo.put("goldenCross", goldenCross);
            alertInfo.put("deathCross", deathCross);
            if (result.has("lastCrossDate")) {
                alertInfo.put("lastCrossDate", result.getString("lastCrossDate"));
            }
            
            stockAlerts.put("movingAverageCrossover", alertInfo);
            
        } catch (IOException e) {
            logger.error("Erreur lors de la vérification des croisements de moyennes mobiles pour {}: {}", 
                    symbol, e.getMessage());
            Map<String, Object> alertInfo = new HashMap<>();
            alertInfo.put("triggered", false);
            alertInfo.put("error", e.getMessage());
            stockAlerts.put("movingAverageCrossover", alertInfo);
        }
    }
    
    private void checkStockIncrease(String symbol, Map<String, Object> stockAlerts) {
        try {
            boolean hasIncreased = stockAlertService.hasStockIncreasedByPercentage(symbol, STOCK_INCREASE_THRESHOLD);
            
            Map<String, Object> alertInfo = new HashMap<>();
            alertInfo.put("triggered", hasIncreased);
            alertInfo.put("threshold", STOCK_INCREASE_THRESHOLD);
            alertInfo.put("message", "L'action " + symbol + (hasIncreased ? " a " : " n'a pas ") + 
                    "augmenté de " + STOCK_INCREASE_THRESHOLD + "% par rapport à la veille.");
            
            stockAlerts.put("stockIncrease", alertInfo);
            
        } catch (IOException e) {
            logger.error("Erreur lors de la vérification de l'augmentation pour {}: {}", symbol, e.getMessage());
            Map<String, Object> alertInfo = new HashMap<>();
            alertInfo.put("triggered", false);
            alertInfo.put("error", e.getMessage());
            stockAlerts.put("stockIncrease", alertInfo);
        }
    }
    
    private void checkAlertThreshold(String symbol, Map<String, Object> stockAlerts) {
        try {
            JSONObject result = alertThresholdService.checkAlert(symbol, ALERT_THRESHOLD);
            
            boolean alertTriggered = result.has("alertTriggered") && result.getBoolean("alertTriggered");
            
            Map<String, Object> alertInfo = new HashMap<>();
            alertInfo.put("triggered", alertTriggered);
            alertInfo.put("threshold", ALERT_THRESHOLD);
            if (result.has("currentPrice")) {
                alertInfo.put("currentPrice", result.getDouble("currentPrice"));
            }
            if (result.has("message")) {
                alertInfo.put("message", result.getString("message"));
            }
            
            stockAlerts.put("thresholdAlert", alertInfo);
            
        } catch (IOException e) {
            logger.error("Erreur lors de la vérification du seuil d'alerte pour {}: {}", symbol, e.getMessage());
            Map<String, Object> alertInfo = new HashMap<>();
            alertInfo.put("triggered", false);
            alertInfo.put("error", e.getMessage());
            stockAlerts.put("thresholdAlert", alertInfo);
        }
    }
    
    private void logAlertResults(Map<String, Map<String, Object>> alertResults) {
        logger.info("==== ALERTES DÉTECTÉES - {} ====", LocalDate.now());
        
        for (Map.Entry<String, Map<String, Object>> entry : alertResults.entrySet()) {
            String symbol = entry.getKey();
            Map<String, Object> alerts = entry.getValue();
            
            logger.info("Action: {}", symbol);
            
            for (Map.Entry<String, Object> alertEntry : alerts.entrySet()) {
                String alertType = alertEntry.getKey();
                Map<String, Object> alertInfo = (Map<String, Object>) alertEntry.getValue();
                
                boolean triggered = (boolean) alertInfo.get("triggered");
                
                if (triggered) {
                    logger.info("  - Alerte {}: DÉCLENCHÉE", alertType);
                    
                    // Log des détails spécifiques à chaque type d'alerte
                    switch (alertType) {
                        case "priceVariation":
                            logger.info("    Variation: {}%, Min: {}, Max: {}, Actuel: {}", 
                                    alertInfo.get("variationPercent"), 
                                    alertInfo.get("minPrice"), 
                                    alertInfo.get("maxPrice"), 
                                    alertInfo.get("currentPrice"));
                            break;
                        case "candlePattern":
                            logger.info("    Motif: {}", alertInfo.get("pattern"));
                            break;
                        case "movingAverageCrossover":
                            logger.info("    Golden Cross: {}, Death Cross: {}", 
                                    alertInfo.get("goldenCross"), 
                                    alertInfo.get("deathCross"));
                            if (alertInfo.containsKey("lastCrossDate")) {
                                logger.info("    Date du dernier croisement: {}", alertInfo.get("lastCrossDate"));
                            }
                            break;
                        case "stockIncrease":
                            logger.info("    Seuil: {}%, Message: {}", 
                                    alertInfo.get("threshold"), 
                                    alertInfo.get("message"));
                            break;
                        case "thresholdAlert":
                            logger.info("    Seuil: {}%, Prix actuel: {}", 
                                    alertInfo.get("threshold"), 
                                    alertInfo.get("currentPrice"));
                            if (alertInfo.containsKey("message")) {
                                logger.info("    Message: {}", alertInfo.get("message"));
                            }
                            break;
                    }
                }
            }
        }
        
        logger.info("================================");
    }
}