package com.traderalerting.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class BoursoramaApiService {

    private final RestTemplate restTemplate;

    @Value("${boursorama.api.url}")
    private String apiUrl;

    public BoursoramaApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getFinancialData(String symbol) {
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .pathSegment("v1", "financial", symbol)
                .toUriString();

        return restTemplate.getForObject(url, String.class);
    }
}
