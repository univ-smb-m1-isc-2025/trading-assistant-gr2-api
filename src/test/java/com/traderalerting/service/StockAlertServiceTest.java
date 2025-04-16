package com.traderalerting.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockAlertServiceTest {

    @Mock
    private YahooFinanceService yahooFinanceService;

    @InjectMocks
    private StockAlertService stockAlertService;

    private JSONObject mockStockHistory;

    @BeforeEach
    void setUp() throws IOException, JSONException {
        // Setup mock response structure
        mockStockHistory = new JSONObject();
        JSONObject chart = new JSONObject();
        JSONArray result = new JSONArray();
        JSONObject resultObj = new JSONObject();
        JSONObject indicators = new JSONObject();
        JSONArray quote = new JSONArray();
        JSONArray closePrices = new JSONArray();

        // Sample prices: yesterday = 100, today = 110 (10% increase)
        closePrices.put(100.0);
        closePrices.put(110.0);

        quote.put(closePrices);
        indicators.put("quote", quote);
        resultObj.put("indicators", indicators);
        result.put(resultObj);
        chart.put("result", result);
        mockStockHistory.put("chart", chart);

        when(yahooFinanceService.getStockHistory(anyString(), anyString()))
                .thenReturn(mockStockHistory);
    }

    @Test
    void hasStockIncreasedByPercentage_shouldReturnTrue_whenIncreaseIsAboveThreshold() throws IOException {
        // Given: 10% increase (from 100 to 110)
        String symbol = "AAPL";
        double percentage = 5.0; // 5% threshold

        // When
        boolean result = stockAlertService.hasStockIncreasedByPercentage(symbol, percentage);

        // Then
        assertTrue(result);
    }

    @Test
    void hasStockIncreasedByPercentage_shouldReturnFalse_whenIncreaseIsBelowThreshold() throws IOException {
        // Given: 10% increase (from 100 to 110)
        String symbol = "AAPL";
        double percentage = 15.0; // 15% threshold

        // When
        boolean result = stockAlertService.hasStockIncreasedByPercentage(symbol, percentage);

        // Then
        assertFalse(result);
    }

    @Test
    void hasStockIncreasedByPercentage_shouldThrowException_whenYahooServiceFails() throws IOException {
        // Given
        String symbol = "AAPL";
        double percentage = 5.0;

        when(yahooFinanceService.getStockHistory(anyString(), anyString()))
                .thenThrow(new IOException("API error"));

        // When/Then
        assertThrows(IOException.class, () -> {
            stockAlertService.hasStockIncreasedByPercentage(symbol, percentage);
        });
    }

    @Test
    void hasStockIncreasedByPercentage_shouldHandleEdgeCases() throws IOException, JSONException {
        // Test with exactly the threshold percentage
        JSONArray closePrices = mockStockHistory.getJSONObject("chart")
                .getJSONArray("result").getJSONObject(0)
                .getJSONObject("indicators").getJSONArray("quote").getJSONArray(0);

        // Set prices for exactly 5% increase (100 -> 105)
        closePrices.put(0, 100.0);
        closePrices.put(1, 105.0);

        assertTrue(stockAlertService.hasStockIncreasedByPercentage("AAPL", 5.0));
    }
}
