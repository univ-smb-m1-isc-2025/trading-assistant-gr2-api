package com.traderalerting.controller;

import com.traderalerting.service.BoursoramaApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BoursoramaController {

    private final BoursoramaApiService boursoramaApiService;

    public BoursoramaController(BoursoramaApiService boursoramaApiService) {
        this.boursoramaApiService = boursoramaApiService;
    }

    @GetMapping("/get-financial-data")
    public String getFinancialData(@RequestParam String symbol) {
        return boursoramaApiService.getFinancialData(symbol);
    }
}
