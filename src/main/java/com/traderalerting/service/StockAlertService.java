package com.traderalerting.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class StockAlertService {

    private final YahooFinanceService yahooFinanceService;

    public StockAlertService(YahooFinanceService yahooFinanceService) {
        this.yahooFinanceService = yahooFinanceService;
    }

    public boolean hasStockIncreasedByPercentage(String symbol, double percentage) throws IOException {
        JSONObject stockHistory = yahooFinanceService.getStockHistory(symbol, "2d");
        JSONArray result = stockHistory.getJSONObject("chart").getJSONArray("result");
        JSONObject firstResult = result.getJSONObject(0);
        JSONObject indicators = firstResult.getJSONObject("indicators");
        JSONArray quoteArray = indicators.getJSONArray("quote");
        JSONObject quote = quoteArray.getJSONObject(0);
        
        // Get the close prices
        JSONArray closePrices = quote.getJSONArray("close");
        
        // Make sure we have at least 2 data points
        if (closePrices.length() < 2) {
            throw new IOException("Not enough historical data available");
        }
        
        // Get the last two close prices
        double yesterdayClose = closePrices.getDouble(closePrices.length() - 2);
        double todayClose = closePrices.getDouble(closePrices.length() - 1);
        
        double increasePercentage = ((todayClose - yesterdayClose) / yesterdayClose) * 100;
        
        return increasePercentage >= percentage;
    }
}