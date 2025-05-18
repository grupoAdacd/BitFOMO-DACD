package es.ulpgc.dacd.bitfomo.binancefeeder.adapters;

import es.ulpgc.dacd.bitfomo.binancefeeder.domain.Candlestick;
import es.ulpgc.dacd.bitfomo.binancefeeder.transformer.JsonDataParser;
import es.ulpgc.dacd.bitfomo.binancefeeder.transformer.CandlestickDeserializer;
import es.ulpgc.dacd.bitfomo.binancefeeder.application.ExchangeDataFetcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

public class ExchangeApiClient extends ExchangeDataFetcher {
    private static long lastKlineTime = -1;
    private static final String LAST_KLINE_FILE = "last_kline_time.txt";
    private static final long DEFAULT_INITIAL_RANGE_MILLIS = 60 * 1000L;
    private long startDateTime;
    private long endDateTime;
    private long intervalMillis = 60 * 1000L;
    private int MAX_ITER = 1;

    public ExchangeApiClient() {
        loadLastKlineTime();
        if (lastKlineTime == -1) {
            startDateTime = System.currentTimeMillis() - DEFAULT_INITIAL_RANGE_MILLIS;
        } else {
            startDateTime = lastKlineTime + 1;
        }
        endDateTime = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private void loadLastKlineTime() {
        try (BufferedReader reader = new BufferedReader(new FileReader(LAST_KLINE_FILE))) {
            String line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                lastKlineTime = Long.parseLong(line.trim());
            }
        } catch (Exception e) {
            System.err.println("Error al leer last_kline_time.txt, usando rango inicial: " + e.getMessage());
            lastKlineTime = -1;
        }
    }

    private void saveLastKlineTime() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LAST_KLINE_FILE))) {
            writer.write(String.valueOf(lastKlineTime));
        } catch (Exception e) {
            System.err.println("Error al escribir last_kline_time.txt: " + e.getMessage());
        }
    }

    public ArrayList<ArrayList<Candlestick>> obtainFullResponse() {
        ArrayList<ArrayList<Candlestick>> fullResponse = new ArrayList<>();
        for (int i = 0; i < MAX_ITER; i++) {
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
                ArrayList<Candlestick> binanceKlineArray = deserializer.deserialize(eachResponse);
                if (binanceKlineArray != null && !binanceKlineArray.isEmpty()) {
                    fullResponse.add(binanceKlineArray);
                    System.out.println("Velas obtenidas: " + binanceKlineArray.size());
                    Candlestick lastKline = binanceKlineArray.get(binanceKlineArray.size() - 1);
                    lastKlineTime = lastKline.getKlineCloseTime();
                    saveLastKlineTime();
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