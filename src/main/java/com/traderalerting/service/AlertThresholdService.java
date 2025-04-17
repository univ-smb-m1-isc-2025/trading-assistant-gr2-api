package com.traderalerting.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AlertThresholdService {

    private final YahooFinanceService yahooFinanceService;

    public AlertThresholdService(YahooFinanceService yahooFinanceService) {
        this.yahooFinanceService = yahooFinanceService;
    }

    public JSONObject checkAlert(String symbol, double threshold) throws IOException {
        // Utiliser le service Yahoo Finance injecté au lieu d'appeler HTTP directement
        JSONObject json = yahooFinanceService.getStockHistory(symbol, "1d");

        JSONArray closePrices = json
                .getJSONObject("chart")
                .getJSONArray("result")
                .getJSONObject(0)
                .getJSONObject("indicators")
                .getJSONArray("quote")
                .getJSONObject(0)
                .getJSONArray("close");

        double latestClose = closePrices.getDouble(closePrices.length() - 1);

        System.out.println("Latest Close Price: " + latestClose);
        System.out.println("Threshold: " + threshold);

        boolean alert = latestClose > threshold;

        JSONObject result = new JSONObject();
        result.put("symbol", symbol);
        result.put("currentPrice", latestClose);
        result.put("threshold", threshold);
        result.put("alert", alert);

        if (alert) {
            result.put("message", "Le prix actuel dépasse le seuil défini.");
        } else {
            result.put("message", "Le prix actuel est en-dessous du seuil défini.");
        }

        return result;
    }
}