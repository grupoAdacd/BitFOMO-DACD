package es.ulpgc.dacd.bitfomo.businessunit2.domain;

import es.ulpgc.dacd.bitfomo.businessunit2.infrastructure.Deduplicator;
import es.ulpgc.dacd.bitfomo.businessunit2.infrastructure.SentimentAnalyzer;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DatamartEnsembler {
    private final String csvPath;
    private final SentimentAnalyzer sentimentAnalyzer;
    private final Deduplicator deduplicator;
    private final Map<Long, RedditData> redditCache = new HashMap<>();
    private final Map<Long, BinanceData> binanceCache = new HashMap<>();
    private final Set<Long> processedRedditTimestamps = new HashSet<>(); // Para rastrear posts ya procesados
    private final long TWO_DAYS_MILLIS = TimeUnit.DAYS.toMillis(2);

    public DatamartEnsembler(String csvPath, SentimentAnalyzer sentimentAnalyzer, Deduplicator deduplicator) {
        this.csvPath = csvPath;
        this.sentimentAnalyzer = sentimentAnalyzer;
        this.deduplicator = deduplicator;
    }

    public void processRedditMessage(JSONObject json) {
        try {
            String postId = json.getString("id");
            if (deduplicator.isDuplicateRedditPost(postId)) {
                System.out.println("Post duplicado ignorado: " + postId);
                return;
            }
            long ts = Instant.parse(json.getString("ts")).toEpochMilli();
            String text = json.optString("title", "") + " " + json.optString("selftext", "");
            double sentiment = sentimentAnalyzer.analyzeSentiment(text);
            redditCache.put(ts, new RedditData(postId, ts, sentiment));
            deduplicator.addRedditPostId(postId);
        } catch (Exception e) {
            System.err.println("Error processing Reddit message: " + e.getMessage());
        }
    }

    public void processBinanceMessage(JSONObject json) {
        try {
            long ts = Instant.parse(json.getString("ts")).toEpochMilli();
            double openPrice = json.getDouble("openPrice");
            double closePrice = json.getDouble("closePrice");
            binanceCache.put(ts, new BinanceData(ts, openPrice, closePrice));
        } catch (Exception e) {
            System.err.println("Error processing Binance message: " + e.getMessage());
        }
    }

    public void assembleDatamart() {
        System.out.println("Ejecutando assembleDatamart a las " + new Date());
        long currentTime = System.currentTimeMillis();
        long timeThreshold = currentTime - TWO_DAYS_MILLIS;
        try (FileWriter writer = new FileWriter(csvPath, true)) {
            for (Map.Entry<Long, RedditData> entry : redditCache.entrySet()) {
                long redditTs = entry.getKey();
                if (redditTs >= timeThreshold && !processedRedditTimestamps.contains(redditTs)) {
                    BinanceData closestBinance = findClosestBinance(redditTs);
                    if (closestBinance != null) {
                        String line = redditTs + "," + entry.getValue().sentiment() + "," +
                                closestBinance.openPrice() + "," + closestBinance.closePrice() + "\n";
                        writer.append(line);
                        processedRedditTimestamps.add(redditTs); // Marcar como procesado
                    }
                }
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
    }

    private BinanceData findClosestBinance(long redditTs) {
        BinanceData closest = null;
        long minDifference = Long.MAX_VALUE;
        long maxRange = TimeUnit.MINUTES.toMillis(5); // Solo considerar datos dentro de 5 minutos
        for (BinanceData binance : binanceCache.values()) {
            long diff = Math.abs(redditTs - binance.ts());
            if (diff < minDifference && diff <= maxRange) {
                minDifference = diff;
                closest = binance;
            }
        }
        if (closest == null) {
            System.out.println("No se encontró dato de Binance cercano para ts: " + redditTs);
        }
        return closest;
    }
}