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
        JSONArray quotes = stockHistory.getJSONObject("chart").getJSONArray("result").getJSONObject(0).getJSONObject("indicators").getJSONArray("quote").getJSONArray(0);

        double yesterdayClose = quotes.getDouble(quotes.length() - 2);
        double todayClose = quotes.getDouble(quotes.length() - 1);

        double increasePercentage = ((todayClose - yesterdayClose) / yesterdayClose) * 100;

        return increasePercentage >= percentage;
    }
}
