package com.traderalerting.controller;

import com.traderalerting.scheduler.CAC40AlertScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scan")
public class ManualScanController {

    @Autowired
    private CAC40AlertScheduler cac40AlertScheduler;

    @PostMapping("/cac40")
    public ResponseEntity<String> triggerManualScan() {
        try {
            cac40AlertScheduler.scanCAC40Stocks();
            return ResponseEntity.ok("Scan des actions du CAC40 déclenché avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors du scan: " + e.getMessage());
        }
    }
}