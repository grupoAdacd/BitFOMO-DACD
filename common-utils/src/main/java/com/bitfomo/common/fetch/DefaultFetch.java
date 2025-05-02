package com.bitfomo.common.fetch;

import com.bitfomo.common.http.DefaultHttpService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DefaultFetch implements DefaultFetchProvider {
    private String apiUrl;
    private String addition;

    public DefaultFetch(String apiUrl, String addition) {
        this.apiUrl = apiUrl;
        this.addition = addition; }

    @Override
    public String fetchInformation() {
        DefaultHttpService httpService = new DefaultHttpService(apiUrl, addition);
        return httpService.sendRequest().body();
    }

    @Override
    public String fetchWhenInformation(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        long startTimeMillis = startDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        long endTimeMillis = endDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        DefaultHttpService httpService = new DefaultHttpService(apiUrl, addition + String.format("&startTime=%d&endTime=%d", startDateTime, endDateTime));
        return httpService.sendRequest().body();
    }
}