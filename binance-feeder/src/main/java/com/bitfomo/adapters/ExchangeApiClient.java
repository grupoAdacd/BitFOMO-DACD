package com.bitfomo.adapters;

import com.bitfomo.domain.CandlestickData;
import com.bitfomo.transformer.JsonDataParser;
import com.bitfomo.transformer.CandlestickDeserializer;
import com.bitfomo.application.ExchangeDataFetcher;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class ExchangeApiClient extends ExchangeDataFetcher {
    private long startDateTime;
    private long endDateTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    private long intervalMillis = 6 * 60 * 60 * 1000;
    private int MAX_ITER = 1;

    public ArrayList<ArrayList<CandlestickData>> obtainFullResponse() {
        ArrayList<ArrayList<CandlestickData>> fullResponse = new ArrayList<>();
        for (int i=0;i<MAX_ITER;i++) {
            MAX_ITER = Math.toIntExact((endDateTime - startDateTime) / intervalMillis);
            String eachResponse = fetchWhenInformation(startDateTime, endDateTime);
            if (eachResponse == null || eachResponse.isEmpty()) {
                break;
            }
            try {
                JsonDataParser jsonArrayOfKlines = new JsonDataParser(eachResponse);
                if (jsonArrayOfKlines.parseArray().isEmpty()) {
                    break;
                }
                CandlestickDeserializer deserializer = new CandlestickDeserializer();
                ArrayList<CandlestickData> binanceKlineArray = deserializer.deserialize(eachResponse);
                if (binanceKlineArray != null && !binanceKlineArray.isEmpty()) {
                    fullResponse.add(binanceKlineArray);
                    CandlestickDBPersistence inserter = new CandlestickDBPersistence();
                    CandlestickData lastKline = binanceKlineArray.get(binanceKlineArray.size() - 1);
                    inserter.setLastKlineIntroduced(lastKline.getKlineCloseTime());
                    setStartDateTime(lastKline.getKlineCloseTime() + 1);
                }
            } catch (Exception e) {
                System.err.println("Error processing response: " + e.getMessage());
                break;
            }
        }
        return fullResponse;
    }

    @Override
    public String fetchWhenInformation(long startTime, long endTime) {
        return super.fetchWhenInformation(startTime, endTime);
    }

    public long getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(long startDateTime) {
        this.startDateTime = startDateTime;
    }

    public long getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(long endDateTime) {
        this.endDateTime = endDateTime;
    }
}