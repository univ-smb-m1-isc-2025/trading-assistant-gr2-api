package com.traderalerting.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class YahooFinanceService {

    private final OkHttpClient client = new OkHttpClient();

    public JSONObject getStockHistory(String symbol, String range) throws IOException {
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol + "?interval=1d&range=" + range;
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur lors de la requête : " + response);
            }

            String responseBody = response.body().string();
            return new JSONObject(responseBody);
        }
    }
}
