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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovingAverageCrossoverServiceTest {

    @Mock
    private YahooFinanceService yahooFinanceService;

    @InjectMocks
    private MovingAverageCrossoverService movingAverageCrossoverService;

    private JSONObject mockHistoricalData;
    private final String TEST_SYMBOL = "AAPL";

    @BeforeEach
    void setUp() throws JSONException {
        // Create mock response with price data and timestamps
        mockHistoricalData = new JSONObject();
        JSONObject chart = new JSONObject();
        JSONArray result = new JSONArray();
        JSONObject resultObj = new JSONObject();
        
        // Add timestamps - one for each day, going back 210 days (to cover 200MA)
        JSONArray timestamps = new JSONArray();
        long currentTime = System.currentTimeMillis() / 1000;
        long dayInSeconds = 24 * 60 * 60;
        
        for (int i = 210; i >= 0; i--) {
            timestamps.put(currentTime - (i * dayInSeconds));
        }
        
        resultObj.put("timestamp", timestamps);
        
        // Add price data
        JSONObject indicators = new JSONObject();
        JSONArray quote = new JSONArray();
        JSONObject quoteObj = new JSONObject();
        
        // Create sample price data with a clear crossover pattern
        JSONArray closePrices = new JSONArray();
        
        // Generate price data with a pattern: 
        // - First 100 days: increasing trend for 10MA above 50MA
        // - Next 50 days: decreasing trend to create a crossover between 10MA and 50MA
        // - Final days: another trend change for crossover between 50MA and 200MA
        
        double basePrice = 100.0;
        
        // First 100 days - uptrend
        for (int i = 0; i < 100; i++) {
            closePrices.put(basePrice + (i * 0.5));
        }
        
        // Next 50 days - downtrend to create 10MA/50MA crossover
        for (int i = 0; i < 50; i++) {
            closePrices.put(basePrice + 50 - (i * 1.2));
        }
        
        // Final days - continued downtrend
        for (int i = 0; i < 61; i++) {
            closePrices.put(basePrice + 10 - (i * 0.3));
        }
        
        quoteObj.put("close", closePrices);
        quote.put(quoteObj);
        indicators.put("quote", quote);
        
        resultObj.put("indicators", indicators);
        result.put(resultObj);
        chart.put("result", result);
        mockHistoricalData.put("chart", chart);
    }

    @Test
    void detectCrossover_shouldDetectCrossovers() throws IOException, JSONException {
        // Arrange
        when(yahooFinanceService.getStockHistory(eq(TEST_SYMBOL), anyString()))
                .thenReturn(mockHistoricalData);
        
        // Act
        JSONObject result = movingAverageCrossoverService.detectCrossover(TEST_SYMBOL, 10, 50, 200, "1y");
        
        // Assert
        assertNotNull(result);
        assertEquals(TEST_SYMBOL, result.getString("symbol"));
        assertEquals(10, result.getInt("shortPeriod"));
        assertEquals(50, result.getInt("mediumPeriod"));
        assertEquals(200, result.getInt("longPeriod"));
        
        // Verify current values exist
        assertTrue(result.has("currentPrice"));
        assertTrue(result.has("currentShortMA"));
        assertTrue(result.has("currentMediumMA"));
        assertTrue(result.has("currentLongMA"));
        assertTrue(result.has("currentTrend"));
        
        // Verify we get crossovers array
        assertTrue(result.has("recentCrossovers"));
        JSONArray crossovers = result.getJSONArray("recentCrossovers");
        
        // Our data was designed to include crossovers
        assertTrue(crossovers.length() > 0, "Should detect at least one crossover");
        
        // Verify service called with correct parameters
        verify(yahooFinanceService).getStockHistory(eq(TEST_SYMBOL), eq("1y"));
    }

    @Test
    void getRecentSignals_shouldReturnSignalsForSpecifiedDays() throws IOException, JSONException {
        // Arrange
        when(yahooFinanceService.getStockHistory(eq(TEST_SYMBOL), eq("30d")))
                .thenReturn(mockHistoricalData);
        
        // Act
        JSONObject result = movingAverageCrossoverService.getRecentSignals(TEST_SYMBOL, 10, 50, 200, 30);
        
        // Assert
        assertNotNull(result);
        assertEquals(TEST_SYMBOL, result.getString("symbol"));
        
        // Verify service called with correct range parameter
        verify(yahooFinanceService).getStockHistory(eq(TEST_SYMBOL), eq("30d"));
    }

    @Test
    void detectCrossover_shouldHandleInsufficientData() throws IOException, JSONException {
        // Arrange - create mock response with insufficient data
        JSONObject insufficientData = new JSONObject();
        JSONObject chart = new JSONObject();
        JSONArray result = new JSONArray();
        JSONObject resultObj = new JSONObject();
        
        // Add minimal timestamps
        JSONArray timestamps = new JSONArray();
        long currentTime = System.currentTimeMillis() / 1000;
        timestamps.put(currentTime);
        timestamps.put(currentTime - 86400);
        resultObj.put("timestamp", timestamps);
        
        // Add minimal price data
        JSONObject indicators = new JSONObject();
        JSONArray quote = new JSONArray();
        JSONObject quoteObj = new JSONObject();
        JSONArray closePrices = new JSONArray();
        closePrices.put(100.0);
        closePrices.put(101.0);
        quoteObj.put("close", closePrices);
        quote.put(quoteObj);
        indicators.put("quote", quote);
        
        resultObj.put("indicators", indicators);
        result.put(resultObj);
        chart.put("result", result);
        insufficientData.put("chart", chart);
        
        when(yahooFinanceService.getStockHistory(eq(TEST_SYMBOL), anyString()))
                .thenReturn(insufficientData);
        
        // Act
        JSONObject resultObj2 = movingAverageCrossoverService.detectCrossover(TEST_SYMBOL, 10, 50, 200, "5d");
        
        // Assert
        assertTrue(resultObj2.has("error"), "Should have error for insufficient data");
    }

    @Test
    void detectCrossover_shouldHandleExceptionFromYahooService() throws IOException {
        // Arrange
        when(yahooFinanceService.getStockHistory(eq(TEST_SYMBOL), anyString()))
                .thenThrow(new IOException("Service error"));
        
        // Act & Assert
        assertThrows(IOException.class, () -> {
            movingAverageCrossoverService.detectCrossover(TEST_SYMBOL, 10, 50, 200, "1y");
        });
    }
}