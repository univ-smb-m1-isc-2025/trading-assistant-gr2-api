package com.traderalerting.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-api")
public class TestApiController {
    
    private static final Logger log = LoggerFactory.getLogger(TestApiController.class);
    
    @GetMapping
    public String test() {
        log.info("Test API endpoint hit");
        return "Test API Controller is working!";
    }
}