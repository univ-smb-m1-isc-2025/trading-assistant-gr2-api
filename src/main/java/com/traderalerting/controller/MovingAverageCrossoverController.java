package com.traderalerting.controller;

import com.traderalerting.service.MovingAverageCrossoverService;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/finance/ma-crossover")
public class MovingAverageCrossoverController {

    private final MovingAverageCrossoverService movingAverageCrossoverService;

    public MovingAverageCrossoverController(MovingAverageCrossoverService movingAverageCrossoverService) {
        this.movingAverageCrossoverService = movingAverageCrossoverService;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<String> detectCrossover(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "10") int shortPeriod,
            @RequestParam(defaultValue = "50") int mediumPeriod,
            @RequestParam(defaultValue = "200") int longPeriod,
            @RequestParam(defaultValue = "1y") String range) {
        
        try {
            JSONObject result = movingAverageCrossoverService.detectCrossover(symbol, shortPeriod, mediumPeriod, longPeriod, range);
            return ResponseEntity.ok(result.toString(4));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/signals/{symbol}")
    public ResponseEntity<String> getRecentSignals(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "10") int shortPeriod,
            @RequestParam(defaultValue = "50") int mediumPeriod,
            @RequestParam(defaultValue = "200") int longPeriod,
            @RequestParam(defaultValue = "30") int days) {
        
        try {
            JSONObject result = movingAverageCrossoverService.getRecentSignals(symbol, shortPeriod, mediumPeriod, longPeriod, days);
            return ResponseEntity.ok(result.toString(4));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}