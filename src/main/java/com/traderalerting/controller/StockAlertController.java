package com.traderalerting.controller;

import com.traderalerting.service.StockAlertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/alerts")
public class StockAlertController {

    private final StockAlertService stockAlertService;

    public StockAlertController(StockAlertService stockAlertService) {
        this.stockAlertService = stockAlertService;
    }

    @GetMapping("/increase/{symbol}")
    public String checkStockIncrease(@PathVariable String symbol, @RequestParam double percentage) {
        try {
            boolean hasIncreased = stockAlertService.hasStockIncreasedByPercentage(symbol, percentage);
            return "Le cours de l'action " + symbol + " a " + (hasIncreased ? "" : "n'a pas ") + "augmenté de " + percentage + "% par rapport à la veille.";
        } catch (IOException e) {
            return "Erreur : " + e.getMessage();
        }
    }
}
