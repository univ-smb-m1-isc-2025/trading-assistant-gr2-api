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
class AlertThresholdServiceTest {

    @Mock
    private YahooFinanceService yahooFinanceService;

    @InjectMocks
    private AlertThresholdService alertThresholdService;

    private JSONObject mockResponse;

    @BeforeEach
    void setUp() throws IOException, JSONException {
        mockResponse = new JSONObject();
        JSONObject chart = new JSONObject();
        JSONArray result = new JSONArray();
        JSONObject resultObj = new JSONObject();
        JSONObject indicators = new JSONObject();
        JSONArray quote = new JSONArray();
        JSONArray close = new JSONArray();

        // Prix d'hier et aujourd'hui
        close.put(98.5);  // yesterday
        close.put(102.0); // today

        JSONObject quoteObject = new JSONObject();
        quoteObject.put("close", close);
        quote.put(quoteObject);

        indicators.put("quote", quote);
        resultObj.put("indicators", indicators);
        result.put(resultObj);
        chart.put("result", result);
        mockResponse.put("chart", chart);
    }

    @Test
    void checkAlert_shouldReturnTrueWhenPriceAboveThreshold() throws IOException, JSONException {
        when(yahooFinanceService.getStockHistory(anyString(), anyString()))
                .thenReturn(mockResponse);

        JSONObject result = alertThresholdService.checkAlert("AAPL", 100.0);

        assertNotNull(result);
        assertEquals("AAPL", result.getString("symbol"));
        assertTrue(result.getBoolean("alert"));
        assertTrue(result.getDouble("currentPrice") > 100.0);
    }

    @Test
    void checkAlert_shouldReturnFalseWhenPriceBelowThreshold() throws IOException, JSONException {
        when(yahooFinanceService.getStockHistory(anyString(), anyString()))
                .thenReturn(mockResponse);

        JSONObject result = alertThresholdService.checkAlert("AAPL", 105.0);

        assertNotNull(result);
        assertEquals("AAPL", result.getString("symbol"));
        assertFalse(result.getBoolean("alert"));
        assertTrue(result.getDouble("currentPrice") < 105.0);
    }

    @Test
    void checkAlert_shouldThrowIOException_whenYahooFails() throws IOException {
        when(yahooFinanceService.getStockHistory(anyString(), anyString()))
                .thenThrow(new IOException("Service error"));

        assertThrows(IOException.class, () -> {
            alertThresholdService.checkAlert("AAPL", 100.0);
        });
    }

    @Test
    void checkAlert_shouldHandleExactThreshold() throws IOException, JSONException {
        when(yahooFinanceService.getStockHistory(anyString(), anyString()))
                .thenReturn(mockResponse);

        // Set close to 100.0
        JSONArray close = mockResponse
                .getJSONObject("chart")
                .getJSONArray("result").getJSONObject(0)
                .getJSONObject("indicators").getJSONArray("quote")
                .getJSONObject(0).getJSONArray("close");

        close.put(1, 100.0); // simulate current price == threshold

        JSONObject result = alertThresholdService.checkAlert("AAPL", 100.0);

        assertNotNull(result);
        assertEquals("AAPL", result.getString("symbol"));
        assertFalse(result.getBoolean("alert")); // not > threshold
    }
}
