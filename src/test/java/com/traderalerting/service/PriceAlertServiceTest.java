package com.traderalerting.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.traderalerting.service.PriceAlertService.PriceVariationResult;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class PriceAlertServiceTest {

    @Mock
    private YahooFinanceService yahooFinanceService;

    @InjectMocks
    private PriceAlertService priceAlertService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testVariationExceedsThreshold() throws IOException, JSONException {
        // test simulant une forte variation
        JSONObject mockResponse = createMockStockData(new double[]{100.0, 105.0, 110.0, 120.0, 130.0});
        
        when(yahooFinanceService.getStockHistory(anyString(), anyString())).thenReturn(mockResponse);
        
        // Appeler le service avec un seuil de 20%
        PriceVariationResult result = priceAlertService.checkPriceVariation("AAPL", 5, 20.0);
        
        // Vérifier les résultats
        assertTrue(result.isExceedsThreshold());
        assertEquals(30.0, result.getVariationPercent(), 0.01);  // 30% de variation
        assertEquals(100.0, result.getMinPrice());
        assertEquals(130.0, result.getMaxPrice());
        assertEquals(130.0, result.getCurrentPrice());
    }

    @Test
    public void testVariationBelowThreshold() throws IOException, JSONException {
        // test simulant une faible variation
        JSONObject mockResponse = createMockStockData(new double[]{100.0, 102.0, 101.0, 103.0, 104.0});
        
        when(yahooFinanceService.getStockHistory(anyString(), anyString())).thenReturn(mockResponse);
        
        // Appeler le service avec un seuil de 10%
        PriceVariationResult result = priceAlertService.checkPriceVariation("MSFT", 5, 10.0);
        
        // Vérifier les résultats
        assertFalse(result.isExceedsThreshold());
        assertEquals(4.0, result.getVariationPercent(), 0.01);  // 4% de variation
        assertEquals(100.0, result.getMinPrice());
        assertEquals(104.0, result.getMaxPrice());
    }

    @Test
    public void testInsufficientData() throws IOException, JSONException {
        // test avec peu de points
        JSONObject mockResponse = createMockStockData(new double[]{100.0});
        
        when(yahooFinanceService.getStockHistory(anyString(), anyString())).thenReturn(mockResponse);
        
        PriceVariationResult result = priceAlertService.checkPriceVariation("GOOG", 5, 10.0);
        
        // Vérifier que le service gère correctement le manque de données
        assertFalse(result.isExceedsThreshold());
        assertTrue(result.getMessage().contains("insuffisantes"));
    }

    @Test
    public void testNullValues() throws IOException, JSONException {
        // Créer un mock avec des valeurs nulles
        JSONObject mockResponseWithNulls = createMockStockDataWithNulls();
        
        when(yahooFinanceService.getStockHistory(anyString(), anyString())).thenReturn(mockResponseWithNulls);
        
        PriceVariationResult result = priceAlertService.checkPriceVariation("AMZN", 5, 15.0);
        
        // Vérifier que le service gère correctement les valeurs nulles
        assertEquals(50.0, result.getVariationPercent(), 0.01);  // 50% de variation
        assertEquals(100.0, result.getMinPrice());
        assertEquals(150.0, result.getMaxPrice());
    }

    /**
     * Crée un objet JSON simulant une réponse de l'API Yahoo Finance
     * @throws JSONException 
     */
    private JSONObject createMockStockData(double[] prices) throws JSONException {
        JSONObject mockResponse = new JSONObject();
        JSONObject chart = new JSONObject();
        JSONArray result = new JSONArray();
        JSONObject resultItem = new JSONObject();
        
        // Créer le tableau de timestamps (dates)
        JSONArray timestamps = new JSONArray();
        long currentTime = System.currentTimeMillis() / 1000;
        for (int i = 0; i < prices.length; i++) {
            timestamps.put(currentTime - (86400 * (prices.length - i - 1)));  // Décrémente d'un jour
        }
        resultItem.put("timestamp", timestamps);
        
        // Créer les indicateurs avec les prix
        JSONObject indicators = new JSONObject();
        JSONArray quote = new JSONArray();
        JSONObject quoteItem = new JSONObject();
        
        JSONArray closeData = new JSONArray();
        for (double price : prices) {
            closeData.put(price);
        }
        
        quoteItem.put("close", closeData);
        quote.put(quoteItem);
        indicators.put("quote", quote);
        
        resultItem.put("indicators", indicators);
        result.put(resultItem);
        chart.put("result", result);
        mockResponse.put("chart", chart);
        
        return mockResponse;
    }

    /**
     * Crée un objet JSON simulant une réponse avec des valeurs nulles
     * @throws JSONException 
     */
    private JSONObject createMockStockDataWithNulls() throws JSONException {
        JSONObject mockResponse = new JSONObject();
        JSONObject chart = new JSONObject();
        JSONArray result = new JSONArray();
        JSONObject resultItem = new JSONObject();
        
        // Timestamps
        JSONArray timestamps = new JSONArray();
        long currentTime = System.currentTimeMillis() / 1000;
        for (int i = 0; i < 5; i++) {  
            timestamps.put(currentTime - (86400 * (5 - i - 1)));
        }
        resultItem.put("timestamp", timestamps);
        
        // Indicateurs avec valeurs nulles
        JSONObject indicators = new JSONObject();
        JSONArray quote = new JSONArray();
        JSONObject quoteItem = new JSONObject();
        
        // Créer directement le JSONArray avec des valeurs nulles
        JSONArray closeData = new JSONArray();
        closeData.put(100.0);
        closeData.put(JSONObject.NULL);
        closeData.put(120.0);
        closeData.put(JSONObject.NULL);
        closeData.put(150.0);
        
        quoteItem.put("close", closeData);
        quote.put(quoteItem);
        indicators.put("quote", quote);
        
        resultItem.put("indicators", indicators);
        result.put(resultItem);
        chart.put("result", result);
        mockResponse.put("chart", chart);
        
        return mockResponse;
    }
}