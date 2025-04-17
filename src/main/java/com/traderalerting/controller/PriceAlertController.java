package com.traderalerting.controller;

import com.traderalerting.service.PriceAlertService;
import com.traderalerting.service.PriceAlertService.PriceVariationResult;

import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/alerts")
public class PriceAlertController {

    private final PriceAlertService priceAlertService;

    public PriceAlertController(PriceAlertService priceAlertService) {
        this.priceAlertService = priceAlertService;
    }

    /**
     * Endpoint pour vérifier si un titre a connu une variation significative
     *
     * @param symbol Le symbole du titre (ex: AAPL, MSFT)
     * @param days Nombre de jours à analyser
     * @param threshold Seuil de variation en pourcentage (ex: 5.0 pour 5%)
     * @return Les résultats de l'analyse
     */
    @GetMapping("/variation/{symbol}")
    public ResponseEntity<?> checkPriceVariation(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "5") int days,
            @RequestParam(defaultValue = "5.0") double threshold) {
        
        try {
            PriceVariationResult result = priceAlertService.checkPriceVariation(symbol, days, threshold);
            
            // Conversion du résultat en JSON
            JSONObject response = new JSONObject();
            response.put("symbol", result.getSymbol());
            response.put("exceedsThreshold", result.isExceedsThreshold());
            response.put("variationPercent", result.getVariationPercent());
            response.put("minPrice", result.getMinPrice());
            response.put("maxPrice", result.getMaxPrice());
            response.put("currentPrice", result.getCurrentPrice());
            response.put("message", result.getMessage());
            
            return ResponseEntity.ok(response.toString(4));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Erreur lors de l'analyse: " + e.getMessage());
        }
    }

    /**
     * Endpoint pour analyser plusieurs titres à la fois
     */
    @PostMapping("/batch-variation")
    public ResponseEntity<?> batchCheckVariation(
            @RequestBody BatchCheckRequest request) {
        
        JSONObject response = new JSONObject();
        
        for (String symbol : request.getSymbols()) {
            try {
                PriceVariationResult result = priceAlertService.checkPriceVariation(
                        symbol, request.getDays(), request.getThreshold());
                
                JSONObject symbolResult = new JSONObject();
                symbolResult.put("exceedsThreshold", result.isExceedsThreshold());
                symbolResult.put("variationPercent", result.getVariationPercent());
                symbolResult.put("minPrice", result.getMinPrice());
                symbolResult.put("maxPrice", result.getMaxPrice());
                symbolResult.put("currentPrice", result.getCurrentPrice());
                symbolResult.put("message", result.getMessage());
                
                response.put(symbol, symbolResult);
            } catch (Exception e) {
                JSONObject errorResult = new JSONObject();
                errorResult.put("error", e.getMessage());
                response.put(symbol, errorResult);
            }
        }
        
        return ResponseEntity.ok(response.toString(4));
    }
    
    /**
     * Classe pour représenter une demande d'analyse par lots
     */
    public static class BatchCheckRequest {
        private String[] symbols;
        private int days = 5;
        private double threshold = 5.0;
        
        public String[] getSymbols() {
            return symbols;
        }
        
        public void setSymbols(String[] symbols) {
            this.symbols = symbols;
        }
        
        public int getDays() {
            return days;
        }
        
        public void setDays(int days) {
            this.days = days;
        }
        
        public double getThreshold() {
            return threshold;
        }
        
        public void setThreshold(double threshold) {
            this.threshold = threshold;
        }
    }
}