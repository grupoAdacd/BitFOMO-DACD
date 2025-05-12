package com.bitfomo.businessunit.adapters;

import com.bitfomo.businessunit.domain.DatamartPort;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CsvDatamart implements DatamartPort {
    private final String csvPath;
    private final Map<String, List<Map<String, Object>>> redditBuffer;
    private final Map<String, List<Map<String, Object>>> cryptoBuffer;
    private final long intervalMinutes = 5;

    public CsvDatamart(String csvPath) {
        this.csvPath = csvPath;
        this.redditBuffer = new HashMap<>();
        this.cryptoBuffer = new HashMap<>();
        initializeCsvFile();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::flushToCsv, 0, intervalMinutes, TimeUnit.MINUTES);
    }

    private void initializeCsvFile() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvPath, true))) {
            if (new java.io.File(csvPath).length() == 0) {
                writer.writeNext(new String[]{"timestamp", "subreddit", "sentiment_score", "symbol", "price"});
            }
        } catch (IOException e) {
            throw new RuntimeException("Error inicializando datamart.csv: " + e.getMessage());
        }
    }

    private String getIntervalTimestamp(String timestamp) {
        Instant instant = Instant.parse(timestamp);
        long minutes = instant.getEpochSecond() / 60;
        long intervalStart = (minutes / intervalMinutes) * intervalMinutes;
        return Instant.ofEpochSecond(intervalStart * 60).toString();
    }

    @Override
    public void storeRedditPost(Map<String, Object> postData) {
        String timestamp = (String) postData.get("ts");
        String interval = getIntervalTimestamp(timestamp);
        redditBuffer.computeIfAbsent(interval, k -> new ArrayList<>()).add(postData);
    }

    @Override
    public void storeCryptoPrice(Map<String, Object> priceData) {
        // Usar el campo ts como timestamp
        String timestamp = (String) priceData.get("ts");
        String interval = getIntervalTimestamp(timestamp);
        cryptoBuffer.computeIfAbsent(interval, k -> new ArrayList<>()).add(priceData);
    }

    @Override
    public void flushToCsv() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvPath, true))) {
            for (String interval : redditBuffer.keySet()) {
                List<Map<String, Object>> posts = redditBuffer.get(interval);
                List<Map<String, Object>> prices = cryptoBuffer.getOrDefault(interval, List.of());

                double sentiment = 0.0;
                String subreddit = "";
                if (!posts.isEmpty()) {
                    subreddit = (String) posts.get(0).get("subreddit");
                    sentiment = posts.stream()
                            .filter(p -> p.get("sentimentScore") != null)
                            .mapToDouble(p -> ((Number) p.get("sentimentScore")).doubleValue())
                            .average()
                            .orElse(0.0);
                }

                double price = 0.0;
                String symbol = "BTC"; // Suponemos que el símbolo es siempre BTC por ahora
                if (!prices.isEmpty()) {
                    price = prices.stream()
                            .filter(p -> p.get("closePrice") != null)
                            .mapToDouble(p -> Double.parseDouble((String) p.get("closePrice")))
                            .average()
                            .orElse(0.0);
                }

                writer.writeNext(new String[]{
                        interval,
                        subreddit,
                        posts.isEmpty() || sentiment == 0.0 ? "" : String.valueOf(sentiment),
                        symbol,
                        prices.isEmpty() ? "" : String.valueOf(price)
                });
            }

            redditBuffer.clear();
            cryptoBuffer.clear();
        } catch (IOException e) {
            System.err.println("Error escribiendo en datamart.csv: " + e.getMessage());
        }
    }

    @Override
    public double getAverageSentiment(String subreddit, String startTime, String endTime) {
        Instant start = Instant.parse(startTime);
        Instant end = Instant.parse(endTime);
        List<Double> scores = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(csvPath))) {
            reader.readNext();
            String[] line;
            while ((line = reader.readNext()) != null) {
                Instant timestamp = Instant.parse(line[0]);
                if (timestamp.isBefore(start) || timestamp.isAfter(end)) {
                    continue;
                }
                if (line[1].equals(subreddit) && !line[2].isEmpty()) {
                    try {
                        double score = Double.parseDouble(line[2]);
                        scores.add(score);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        } catch (IOException | CsvValidationException e) {
            System.err.println("Error leyendo datamart.csv: " + e.getMessage());
        }

        if (scores.isEmpty()) {
            return 0.0;
        }
        return scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    @Override
    public double getAveragePrice(String symbol, String startTime, String endTime) {
        Instant start = Instant.parse(startTime);
        Instant end = Instant.parse(endTime);
        List<Double> prices = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(csvPath))) {
            reader.readNext();
            String[] line;
            while ((line = reader.readNext()) != null) {
                Instant timestamp = Instant.parse(line[0]);
                if (timestamp.isBefore(start) || timestamp.isAfter(end)) {
                    continue;
                }
                if (line[3].equals(symbol) && !line[4].isEmpty()) {
                    try {
                        double price = Double.parseDouble(line[4]);
                        prices.add(price);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        } catch (IOException | CsvValidationException e) {
            System.err.println("Error leyendo datamart.csv: " + e.getMessage());
        }

        if (prices.isEmpty()) {
            return 0.0;
        }
        return prices.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}