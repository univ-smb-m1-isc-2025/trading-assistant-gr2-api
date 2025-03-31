package com.traderalerting.controller;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import com.traderalerting.service.YahooFinanceService;

import java.io.IOException;

@RestController
@RequestMapping("/finance")
public class YahooFinanceController {

    private final YahooFinanceService yahooFinanceService;

    public YahooFinanceController(YahooFinanceService yahooFinanceService) {
        this.yahooFinanceService = yahooFinanceService;
    }

    @GetMapping("/history/{symbol}")
    public String getStockHistory(@PathVariable String symbol, @RequestParam String range) {
        try {
            // Pass the range parameter to the service method
            JSONObject data = yahooFinanceService.getStockHistory(symbol, range);
            return data.toString(4); // Format JSON bien affiché
        } catch (IOException e) {
            return "Erreur : " + e.getMessage();
        }
    }
}
