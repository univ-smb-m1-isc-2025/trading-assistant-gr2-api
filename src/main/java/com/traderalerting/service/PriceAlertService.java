package com.traderalerting.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PriceAlertService {

    private final YahooFinanceService yahooFinanceService;

    public PriceAlertService(YahooFinanceService yahooFinanceService) {
        this.yahooFinanceService = yahooFinanceService;
    }

    /**
     * Vérifie si le cours a eu une variation supérieure à un certain pourcentage
     * pendant les derniers jours.
     * 
     * @param symbol Le symbole de l'action
     * @param days Nombre de jours à analyser
     * @param thresholdPercent Pourcentage de variation à détecter
     * @return Résultat de l'analyse avec des informations détaillées
     * @throws IOException En cas d'erreur lors de la récupération des données
     */
    public PriceVariationResult checkPriceVariation(String symbol, int days, double thresholdPercent) throws IOException {
        // Déterminer la plage pour Yahoo Finance basée sur le nombre de jours
        String range = days <= 5 ? "5d" : 
                      days <= 30 ? "1mo" : 
                      days <= 90 ? "3mo" : "6mo";
        
        JSONObject stockData = yahooFinanceService.getStockHistory(symbol, range);
        
        // Extraction des données de prix de clôture
        JSONObject result = stockData.getJSONObject("chart").getJSONArray("result").getJSONObject(0);
        JSONArray timestamps = result.getJSONArray("timestamp");
        JSONObject indicators = result.getJSONObject("indicators");
        JSONArray closeData = indicators.getJSONArray("quote").getJSONObject(0).getJSONArray("close");
        
        if (closeData.length() < days) {
            return new PriceVariationResult(symbol, false, 0, 0, 0, 
                    "Données insuffisantes: " + closeData.length() + " jours disponibles sur " + days + " requis");
        }
        
        // Extraction des prix pertinents pour la période spécifiée
        List<Double> prices = new ArrayList<>();
        int dataPoints = Math.min(days, closeData.length());
        
        // On prend les X derniers jours à partir de la fin du tableau
        for (int i = closeData.length() - dataPoints; i < closeData.length(); i++) {
            if (!closeData.isNull(i)) {
                prices.add(closeData.getDouble(i));
            }
        }
        
        if (prices.size() < 2) {
            return new PriceVariationResult(symbol, false, 0, 0, 0, 
                    "Données valides insuffisantes après filtrage des valeurs nulles");
        }
        
        // Calcul des prix min et max sur la période
        double minPrice = Double.MAX_VALUE;
        double maxPrice = Double.MIN_VALUE;
        
        for (Double price : prices) {
            if (price < minPrice) minPrice = price;
            if (price > maxPrice) maxPrice = price;
        }
        
        // Calcul de la variation en pourcentage
        double variation = ((maxPrice - minPrice) / minPrice) * 100;
        
        // Déterminer si la variation dépasse le seuil
        boolean exceedsThreshold = variation >= thresholdPercent;
        
        // Prix actuel (dernier prix disponible)
        double currentPrice = prices.get(prices.size() - 1);
        
        return new PriceVariationResult(
            symbol,
            exceedsThreshold,
            variation,
            minPrice,
            maxPrice,
            currentPrice,
            "Analyse sur " + prices.size() + " jours"
        );
    }
    
    /**
     * Classe interne pour représenter le résultat de l'analyse de variation
     */
    public static class PriceVariationResult {
        private final String symbol;
        private final boolean exceedsThreshold;
        private final double variationPercent;
        private final double minPrice;
        private final double maxPrice;
        private final double currentPrice;
        private final String message;
        
        public PriceVariationResult(String symbol, boolean exceedsThreshold, 
                                   double variationPercent, double minPrice, 
                                   double maxPrice, String message) {
            this(symbol, exceedsThreshold, variationPercent, minPrice, maxPrice, maxPrice, message);
        }
        
        public PriceVariationResult(String symbol, boolean exceedsThreshold, 
                                   double variationPercent, double minPrice, 
                                   double maxPrice, double currentPrice, String message) {
            this.symbol = symbol;
            this.exceedsThreshold = exceedsThreshold;
            this.variationPercent = variationPercent;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.currentPrice = currentPrice;
            this.message = message;
        }
        
        public String getSymbol() {
            return symbol;
        }
        
        public boolean isExceedsThreshold() {
            return exceedsThreshold;
        }
        
        public double getVariationPercent() {
            return variationPercent;
        }
        
        public double getMinPrice() {
            return minPrice;
        }
        
        public double getMaxPrice() {
            return maxPrice;
        }
        
        public double getCurrentPrice() {
            return currentPrice;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return "PriceVariationResult{" +
                    "symbol='" + symbol + '\'' +
                    ", exceedsThreshold=" + exceedsThreshold +
                    ", variationPercent=" + variationPercent +
                    ", minPrice=" + minPrice +
                    ", maxPrice=" + maxPrice +
                    ", currentPrice=" + currentPrice +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}