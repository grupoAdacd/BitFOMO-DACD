package com.bitfomo.businessunit.adapters;

import com.bitfomo.businessunit.domain.DatamartPort;
//import com.bitfomo.domain.CryptoPrice;
import com.bitfomo.domain.RedditPost;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CsvDatamart implements DatamartPort {
    private final String csvPath;
    private final Map<String, List<RedditPost>> redditBuffer; // Buffer para posts de Reddit por intervalo
    //private final Map<String, List<CryptoPrice>> cryptoBuffer; // Buffer para precios de Binance por intervalo
    private final long intervalMinutes = 5; // Intervalo de 5 minutos

    public CsvDatamart(String csvPath) {
        this.csvPath = csvPath;
        this.redditBuffer = new HashMap<>();
        //this.cryptoBuffer = new HashMap<>();
        initializeCsvFile();

        // Programa la escritura al CSV cada 5 minutos
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
        // Convierte el timestamp a un intervalo de 5 minutos
        Instant instant = Instant.parse(timestamp);
        long minutes = instant.getEpochSecond() / 60;
        long intervalStart = (minutes / intervalMinutes) * intervalMinutes;
        return Instant.ofEpochSecond(intervalStart * 60).toString();
    }

    @Override
    public void storeRedditPost(RedditPost post) {
        String interval = getIntervalTimestamp(post.ts());
        redditBuffer.computeIfAbsent(interval, k -> new ArrayList<>()).add(post);
    }

//    @Override
//    public void storeCryptoPrice(CryptoPrice price) {
//        String interval = getIntervalTimestamp(price.ts());
//        cryptoBuffer.computeIfAbsent(interval, k -> new ArrayList<>()).add(price);
//    }

    @Override
    public void flushToCsv() {
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvPath, true))) {
            for (String interval : redditBuffer.keySet()) {
                List<RedditPost> posts = redditBuffer.get(interval);
                //List<CryptoPrice> prices = cryptoBuffer.getOrDefault(interval, List.of());

                // Calcula el sentimiento promedio
                double sentiment = 0.0;
                String subreddit = "";
                if (!posts.isEmpty()) {
                    subreddit = posts.get(0).subreddit();
                    sentiment = posts.stream()
                            .filter(p -> p.sentimentScore() != null)
                            .mapToDouble(RedditPost::sentimentScore)
                            .average()
                            .orElse(0.0);
                }

                // Calcula el precio promedio
//                double price = 0.0;
//                String symbol = "";
//                if (!prices.isEmpty()) {
//                    symbol = prices.get(0).symbol();
//                    price = prices.stream()
//                            .mapToDouble(CryptoPrice::price)
//                            .average()
//                            .orElse(0.0);
//                }

                // Escribe la fila en el CSV
                writer.writeNext(new String[]{
                        interval,
                        subreddit,
                        posts.isEmpty() ? "" : String.valueOf(sentiment),
                        //symbol,
                        //prices.isEmpty() ? "" : String.valueOf(price)
                });
            }

            // Limpia los buffers
            redditBuffer.clear();
            //cryptoBuffer.clear();
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
            reader.readNext(); // Saltar el encabezado
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
                        // Ignorar valores no válidos
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
            reader.readNext(); // Saltar el encabezado
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
                        // Ignorar valores no válidos
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