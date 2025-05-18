package es.ulpgc.dacd.bitfomo.businessunit.infrastructure;

import es.ulpgc.dacd.bitfomo.businessunit.domain.Entry;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RecommendationCalculator {
    private final long timeWindowMillis = TimeUnit.HOURS.toMillis(48);
    private final long recentWindowMillis = TimeUnit.HOURS.toMillis(6);
    private final double decayFactor = 0.1;

    public String generateRecommendation(String csvPath) {
        long currentTimeMs = System.currentTimeMillis();
        long timeThreshold = currentTimeMs - timeWindowMillis;
        long recentThreshold = currentTimeMs - recentWindowMillis;
        List<Entry> entries = readEntries(csvPath, timeThreshold);
        if (entries.isEmpty()) {
            return "No hay datos disponibles para generar una recomendación.";
        }
        return buildRecommendation(entries, currentTimeMs, recentThreshold);
    }

    private List<Entry> readEntries(String csvPath, long timeThreshold) {
        List<Entry> entries = new ArrayList<>();
        try (var reader = new java.io.BufferedReader(new java.io.FileReader(csvPath))) {
            reader.readLine(); // Saltar el encabezado
            addEntries(reader, timeThreshold, entries);
        } catch (Exception e) {
            System.err.println("Error leyendo CSV: " + e.getMessage());
        }
        return entries;
    }

    private void addEntries(java.io.BufferedReader reader, long timeThreshold, List<Entry> entries) throws java.io.IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.split(",").length == 4) {
                processLine(line, timeThreshold, entries);
            }
        }
    }

    private void processLine(String line, long timeThreshold, List<Entry> entries) {
        String[] parts = line.split(",");
        long ts = Long.parseLong(parts[0]);
        if (ts >= timeThreshold) {
            double sentiment = Double.parseDouble(parts[1]);
            double priceDiff = Double.parseDouble(parts[3]) - Double.parseDouble(parts[2]);
            entries.add(new Entry(ts, sentiment, priceDiff));
        }
    }

    private String buildRecommendation(List<Entry> entries, long currentTimeMs, long recentThreshold) {
        WeightedStats stats = calculateWeightedStats(entries, currentTimeMs);
        WeightedStats recentStats = calculateWeightedStats(filterRecentEntries(entries, recentThreshold), currentTimeMs);
        double volatility = calculateVolatility(entries);
        String dateTime = LocalDateTime.now(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("hh:mm a 'UTC on' EEEE, MMMM d, yyyy"));
        return formatRecommendation(stats, recentStats, volatility, dateTime);
    }

    private List<Entry> filterRecentEntries(List<Entry> entries, long recentThreshold) {
        List<Entry> recentEntries = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry.ts() >= recentThreshold) {
                recentEntries.add(entry);
            }
        }
        return recentEntries;
    }

    private WeightedStats calculateWeightedStats(List<Entry> entries, long currentTimeMs) {
        WeightedStats stats = new WeightedStats();
        for (Entry entry : entries) {
            double weight = Math.exp(-decayFactor * (currentTimeMs - entry.ts()) / (double) timeWindowMillis);
            stats.addEntry(entry.sentiment(), entry.priceDiff(), weight);
        }
        return stats;
    }

    private double calculateVolatility(List<Entry> entries) {
        double meanSentiment = entries.stream().mapToDouble(Entry::sentiment).average().orElse(0.0);
        double meanPriceDiff = entries.stream().mapToDouble(Entry::priceDiff).average().orElse(0.0);
        double sentimentVariance = entries.stream().mapToDouble(e -> Math.pow(e.sentiment() - meanSentiment, 2)).average().orElse(0.0);
        double priceDiffVariance = entries.stream().mapToDouble(e -> Math.pow(e.priceDiff() - meanPriceDiff, 2)).average().orElse(0.0);
        return Math.sqrt(sentimentVariance + priceDiffVariance);
    }

    private String formatRecommendation(WeightedStats stats, WeightedStats recentStats, double volatility, String dateTime) {
        double avgSentiment = stats.getWeightedSentiment();
        double avgPriceDiff = stats.getWeightedPriceDiff();
        double recentSentimentChange = recentStats.getWeightedSentiment() - avgSentiment;
        double recentPriceChange = recentStats.getWeightedPriceDiff() - avgPriceDiff;
        String recommendation = determineRecommendation(avgSentiment, avgPriceDiff, recentSentimentChange, recentPriceChange, volatility);
        return String.format("Recomendación generada a las %s:%nSentimiento medio (ponderado): %.2f%n" +
                        "Media de la diferencia de precios (ponderada): %.2f%nTendencia reciente (sentimiento): %.2f%n" +
                        "Tendencia reciente (precio): %.2f%nVolatilidad: %.2f%nRecomendación: %s",
                dateTime, avgSentiment, avgPriceDiff, recentSentimentChange, recentPriceChange, volatility, recommendation);
    }

    private String determineRecommendation(double avgSentiment, double avgPriceDiff,
                                           double recentSentimentChange, double recentPriceChange, double volatility) {
        boolean isPriceUp = avgPriceDiff > 0;
        boolean isRecentSentimentUp = recentSentimentChange > 0;
        boolean isRecentPriceUp = recentPriceChange > 0;
        boolean highVolatility = volatility > 0.5;

        if (avgSentiment > -0.1 && avgSentiment < 0.1) {
            return highVolatility ? "Mantener (alta volatilidad)" : "Mantener";
        }
        if (avgSentiment >= 0.1) {
            if (highVolatility && !isRecentSentimentUp) return "Mantener (alta volatilidad)";
            return isRecentPriceUp ? "Comprar" : "Mantener";
        }
        if (highVolatility && !isRecentSentimentUp) return "Mantener (alta volatilidad)";
        return isPriceUp ? "Mantener" : "Vender";
    }
}