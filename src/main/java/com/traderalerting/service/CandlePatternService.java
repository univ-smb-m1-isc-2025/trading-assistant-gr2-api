package com.traderalerting.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CandlePatternService {

    private final YahooFinanceService yahooFinanceService;

    public CandlePatternService(YahooFinanceService yahooFinanceService) {
        this.yahooFinanceService = yahooFinanceService;
    }

    public List<String> detectDragonflyDoji(String symbol, String range) throws IOException {
        JSONObject json = yahooFinanceService.getStockHistory(symbol, range);

        JSONObject result = json.getJSONObject("chart").getJSONArray("result").getJSONObject(0);
        JSONArray timestamps = result.getJSONArray("timestamp");
        JSONObject indicators = result.getJSONObject("indicators").getJSONArray("quote").getJSONObject(0);

        JSONArray opens = indicators.getJSONArray("open");
        JSONArray closes = indicators.getJSONArray("close");
        JSONArray highs = indicators.getJSONArray("high");
        JSONArray lows = indicators.getJSONArray("low");

        List<String> matchingDates = new ArrayList<>();

        for (int i = 0; i < timestamps.length(); i++) {
            double open = opens.optDouble(i, Double.NaN);
            double close = closes.optDouble(i, Double.NaN);
            double high = highs.optDouble(i, Double.NaN);
            double low = lows.optDouble(i, Double.NaN);

            // Skip if data is invalid
            if (Double.isNaN(open) || Double.isNaN(close) || Double.isNaN(high) || Double.isNaN(low)) {
                continue;
            }

            double bodySize = Math.abs(open - close);
            double totalRange = high - low;
            double upperWick = high - Math.max(open, close);
            double lowerWick = Math.min(open, close) - low;

            if (
                bodySize <= (totalRange * 0.1) &&         // Petit corps
                lowerWick >= (totalRange * 0.6) &&        // Longue mèche basse
                upperWick <= (totalRange * 0.1)           // Petite ou aucune mèche haute
            ) {
                long timestamp = timestamps.getLong(i);
                matchingDates.add(java.time.Instant.ofEpochSecond(timestamp).toString());
            }
        }

        return matchingDates;
    }
}
