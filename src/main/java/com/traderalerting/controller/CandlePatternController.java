package com.traderalerting.controller;

import com.traderalerting.service.CandlePatternService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/finance")
public class CandlePatternController {

    private final CandlePatternService candlePatternService;

    public CandlePatternController(CandlePatternService candlePatternService) {
        this.candlePatternService = candlePatternService;
    }

    @GetMapping("/dragonfly-doji/{symbol}")
    public List<String> getDragonflyDojiDates(
            @PathVariable String symbol,
            @RequestParam String range) {
        try {
            return candlePatternService.detectDragonflyDoji(symbol, range);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'analyse des données : " + e.getMessage());
        }
    }
}
