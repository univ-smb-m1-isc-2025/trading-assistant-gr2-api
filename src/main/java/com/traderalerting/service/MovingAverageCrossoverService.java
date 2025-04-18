package com.traderalerting.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovingAverageCrossoverService {

    private final YahooFinanceService yahooFinanceService;

    public MovingAverageCrossoverService(YahooFinanceService yahooFinanceService) {
        this.yahooFinanceService = yahooFinanceService;
    }

    public JSONObject detectCrossover(String symbol, int shortPeriod, int mediumPeriod, int longPeriod, String range) 
            throws IOException {
        
        // Fetch historical data with enough data for the longest period
        JSONObject stockData = yahooFinanceService.getStockHistory(symbol, range);
        
        // Parse timestamp and close prices
        JSONArray timestamps = stockData
                .getJSONObject("chart")
                .getJSONArray("result")
                .getJSONObject(0)
                .getJSONArray("timestamp");
                
        JSONArray closePrices = stockData
                .getJSONObject("chart")
                .getJSONArray("result")
                .getJSONObject(0)
                .getJSONObject("indicators")
                .getJSONArray("quote")
                .getJSONObject(0)
                .getJSONArray("close");
        
        // Calculate moving averages
        List<Double> shortMA = calculateMovingAverage(closePrices, shortPeriod);
        List<Double> mediumMA = calculateMovingAverage(closePrices, mediumPeriod);
        List<Double> longMA = calculateMovingAverage(closePrices, longPeriod);
        
        // Check for crossover in recent data
        JSONObject result = new JSONObject();
        result.put("symbol", symbol);
        result.put("shortPeriod", shortPeriod);
        result.put("mediumPeriod", mediumPeriod);
        result.put("longPeriod", longPeriod);
        
        // Check crossovers starting from the most recent data point going back
        // We need at least 2 points to check for a crossover
        int validDataPoints = Math.min(shortMA.size(), Math.min(mediumMA.size(), longMA.size()));
        
        if (validDataPoints < 2) {
            result.put("error", "Not enough data to detect crossovers");
            return result;
        }
        
        JSONArray crossoversArray = new JSONArray();
        
        // Check for crossovers in the last 5 days
        for (int i = validDataPoints - 1; i > 0; i--) {
            int dateIndex = (closePrices.length() - validDataPoints) + i;
            
            // Short MA crossing above Medium MA
            if (shortMA.get(i - 1) <= mediumMA.get(i - 1) && shortMA.get(i) > mediumMA.get(i)) {
                JSONObject crossover = new JSONObject();
                crossover.put("type", "short-above-medium");
                crossover.put("signal", "bullish");
                crossover.put("date", formatTimestamp(timestamps.getLong(dateIndex)));
                crossover.put("shortMA", shortMA.get(i));
                crossover.put("mediumMA", mediumMA.get(i));
                crossoversArray.put(crossover);
            }
            
            // Short MA crossing below Medium MA
            if (shortMA.get(i - 1) >= mediumMA.get(i - 1) && shortMA.get(i) < mediumMA.get(i)) {
                JSONObject crossover = new JSONObject();
                crossover.put("type", "short-below-medium");
                crossover.put("signal", "bearish");
                crossover.put("date", formatTimestamp(timestamps.getLong(dateIndex)));
                crossover.put("shortMA", shortMA.get(i));
                crossover.put("mediumMA", mediumMA.get(i));
                crossoversArray.put(crossover);
            }
            
            // Short MA crossing above Long MA (Golden Cross)
            if (shortMA.get(i - 1) <= longMA.get(i - 1) && shortMA.get(i) > longMA.get(i)) {
                JSONObject crossover = new JSONObject();
                crossover.put("type", "golden-cross");
                crossover.put("signal", "strongly bullish");
                crossover.put("date", formatTimestamp(timestamps.getLong(dateIndex)));
                crossover.put("shortMA", shortMA.get(i));
                crossover.put("longMA", longMA.get(i));
                crossoversArray.put(crossover);
            }
            
            // Short MA crossing below Long MA (Death Cross)
            if (shortMA.get(i - 1) >= longMA.get(i - 1) && shortMA.get(i) < longMA.get(i)) {
                JSONObject crossover = new JSONObject();
                crossover.put("type", "death-cross");
                crossover.put("signal", "strongly bearish");
                crossover.put("date", formatTimestamp(timestamps.getLong(dateIndex)));
                crossover.put("shortMA", shortMA.get(i));
                crossover.put("longMA", longMA.get(i));
                crossoversArray.put(crossover);
            }
            
            // Medium MA crossing above Long MA
            if (mediumMA.get(i - 1) <= longMA.get(i - 1) && mediumMA.get(i) > longMA.get(i)) {
                JSONObject crossover = new JSONObject();
                crossover.put("type", "medium-above-long");
                crossover.put("signal", "bullish");
                crossover.put("date", formatTimestamp(timestamps.getLong(dateIndex)));
                crossover.put("mediumMA", mediumMA.get(i));
                crossover.put("longMA", longMA.get(i));
                crossoversArray.put(crossover);
            }
            
            // Medium MA crossing below Long MA
            if (mediumMA.get(i - 1) >= longMA.get(i - 1) && mediumMA.get(i) < longMA.get(i)) {
                JSONObject crossover = new JSONObject();
                crossover.put("type", "medium-below-long");
                crossover.put("signal", "bearish");
                crossover.put("date", formatTimestamp(timestamps.getLong(dateIndex)));
                crossover.put("mediumMA", mediumMA.get(i));
                crossover.put("longMA", longMA.get(i));
                crossoversArray.put(crossover);
            }
            
            // Only check last 5 crossovers
            if (crossoversArray.length() >= 5) {
                break;
            }
        }
        
        // Current Values
        result.put("currentPrice", closePrices.getDouble(closePrices.length() - 1));
        result.put("currentShortMA", shortMA.get(shortMA.size() - 1));
        result.put("currentMediumMA", mediumMA.get(mediumMA.size() - 1));
        result.put("currentLongMA", longMA.get(longMA.size() - 1));
        
        // Market trend analysis
        String trend = "neutral";
        if (shortMA.get(shortMA.size() - 1) > mediumMA.get(mediumMA.size() - 1) && 
            mediumMA.get(mediumMA.size() - 1) > longMA.get(longMA.size() - 1)) {
            trend = "bullish";
        } else if (shortMA.get(shortMA.size() - 1) < mediumMA.get(mediumMA.size() - 1) && 
                  mediumMA.get(mediumMA.size() - 1) < longMA.get(longMA.size() - 1)) {
            trend = "bearish";
        }
        
        result.put("currentTrend", trend);
        result.put("recentCrossovers", crossoversArray);
        
        return result;
    }
    
    public JSONObject getRecentSignals(String symbol, int shortPeriod, int mediumPeriod, int longPeriod, int days) 
            throws IOException {
        
        // Get sufficient historical data for analysis
        String range = days + "d";
        return detectCrossover(symbol, shortPeriod, mediumPeriod, longPeriod, range);
    }
    
    private List<Double> calculateMovingAverage(JSONArray prices, int period) {
        List<Double> movingAverages = new ArrayList<>();
        
        if (period > prices.length()) {
            return movingAverages; 
        }
        
        for (int i = period - 1; i < prices.length(); i++) {
            double sum = 0;
            int validPoints = 0;
            
            for (int j = 0; j < period; j++) {
                if (!prices.isNull(i - j)) {
                    sum += prices.getDouble(i - j);
                    validPoints++;
                }
            }
            
            if (validPoints > 0) {
                movingAverages.add(sum / validPoints);
            } else {
                movingAverages.add(null); 
            }
        }
        
        return movingAverages;
    }
    
    private String formatTimestamp(long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}