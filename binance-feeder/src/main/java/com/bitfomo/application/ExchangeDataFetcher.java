package com.bitfomo.application;


import com.bitfomo.domain.ExchangeDataProvider;
import com.bitfomo.adapters.HttpClient;

public class ExchangeDataFetcher implements ExchangeDataProvider {
    private final String Api_url = "https://api.binance.com/api/v3/uiKlines?symbol=";
    private String Symbol = "BTCUSDT";
    private final String Interval = "&interval=6h";
    private final String Limit = "&limit=1000";
    private final String Addition = Symbol + Interval + Limit;

    @Override
    public String fetchWhenInformation(long startDateTime, long endDateTime) {
        HttpClient httpService = new HttpClient(Api_url, Addition + String.format("&startTime=%d&endTime=%d", startDateTime, endDateTime));
        return httpService.sendRequest().body();
    }
}
