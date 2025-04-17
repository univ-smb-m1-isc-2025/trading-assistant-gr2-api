package com.traderalerting.controller;

import com.traderalerting.service.AlertThresholdService;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/alerts")
public class AlertThresholdController {

    private final AlertThresholdService alertThresholdService;

    public AlertThresholdController(AlertThresholdService alertThresholdService) {
        this.alertThresholdService = alertThresholdService;
    }

    @GetMapping("/seuil/{symbol}")
    public ResponseEntity<String> checkAlert(@PathVariable String symbol, @RequestParam double threshold) {
        try {
            JSONObject result = alertThresholdService.checkAlert(symbol, threshold);
            return ResponseEntity.ok(result.toString(4)); // formaté pour affichage
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Erreur : " + e.getMessage());
        }
    }
}
