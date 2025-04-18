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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandlePatternServiceTest {

    @Mock
    private YahooFinanceService yahooFinanceService;

    @InjectMocks
    private CandlePatternService candlePatternService;

    private JSONObject mockCandleData;
    private final String TEST_SYMBOL = "AAPL";

    @BeforeEach
    void setUp() throws JSONException {
        mockCandleData = new JSONObject();
        JSONObject chart = new JSONObject();
        JSONArray resultArray = new JSONArray();
        JSONObject resultObj = new JSONObject();

        // Mock timestamps
        JSONArray timestamps = new JSONArray();
        long now = System.currentTimeMillis() / 1000;
        for (int i = 0; i < 5; i++) {
            timestamps.put(now - i * 86400); // one per day
        }

        // Mock candle data
        JSONArray opens = new JSONArray();
        JSONArray closes = new JSONArray();
        JSONArray highs = new JSONArray();
        JSONArray lows = new JSONArray();

        // Add 5 candles, 1 with Dragonfly Doji characteristics
        opens.put(100.0); closes.put(100.2); highs.put(100.5); lows.put(99.0);  // Normal
        opens.put(102.0); closes.put(102.0); highs.put(102.1); lows.put(99.5);  // Dragonfly Doji
        opens.put(105.0); closes.put(104.5); highs.put(106.0); lows.put(103.0); // Normal
        opens.put(101.0); closes.put(101.5); highs.put(102.0); lows.put(100.0); // Normal
        opens.put(103.0); closes.put(102.8); highs.put(104.0); lows.put(101.0); // Normal

        JSONObject quoteObj = new JSONObject();
        quoteObj.put("open", opens);
        quoteObj.put("close", closes);
        quoteObj.put("high", highs);
        quoteObj.put("low", lows);

        JSONArray quoteArray = new JSONArray();
        quoteArray.put(quoteObj);

        resultObj.put("timestamp", timestamps);
        JSONObject indicators = new JSONObject();
        indicators.put("quote", quoteArray);
        resultObj.put("indicators", indicators);

        resultArray.put(resultObj);
        chart.put("result", resultArray);
        mockCandleData.put("chart", chart);
    }

    @Test
    void detectDragonflyDoji_shouldReturnMatchingDates() throws IOException {
        // Arrange
        when(yahooFinanceService.getStockHistory(eq(TEST_SYMBOL), eq("5d"))).thenReturn(mockCandleData);

        // Act
        List<String> matches = candlePatternService.detectDragonflyDoji(TEST_SYMBOL, "5d");

        // Assert
        assertNotNull(matches);
        assertFalse(matches.isEmpty(), "Should detect at least one Dragonfly Doji");
        assertEquals(1, matches.size()); 
    }

    @Test
    void detectDragonflyDoji_shouldHandleEmptyData() throws IOException, JSONException {
        // Arrange
        JSONObject emptyData = new JSONObject();
        emptyData.put("chart", new JSONObject().put("result", new JSONArray()));

        when(yahooFinanceService.getStockHistory(eq(TEST_SYMBOL), eq("5d"))).thenReturn(emptyData);

        // Act & Assert
        assertThrows(Exception.class, () -> candlePatternService.detectDragonflyDoji(TEST_SYMBOL, "5d"));
    }

    @Test
    void detectDragonflyDoji_shouldHandleServiceError() throws IOException {
        // Arrange
        when(yahooFinanceService.getStockHistory(eq(TEST_SYMBOL), eq("5d")))
                .thenThrow(new IOException("Service unavailable"));

        // Act & Assert
        assertThrows(IOException.class, () -> candlePatternService.detectDragonflyDoji(TEST_SYMBOL, "5d"));
    }
}
