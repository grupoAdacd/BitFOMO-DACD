package es.ulpgc.dacd.bitfomo.businessunit.infrastructure;

import java.util.concurrent.TimeUnit;

public class RecommendationCalculator {
    private final long timeWindowMillis = TimeUnit.HOURS.toMillis(48);

    public String generateRecommendation(String csvPath) {
        long currentTimeMs = System.currentTimeMillis();
        long timeThreshold = currentTimeMs - timeWindowMillis;
        RecommendationStats stats = calculateStats(csvPath, timeThreshold);
        return stats.count() == 0 ? "No hay datos disponibles." : buildRecommendation(stats);
    }

    private RecommendationStats calculateStats(String csvPath, long timeThreshold) {
        RecommendationStats stats = new RecommendationStats();
        try (var reader = new java.io.BufferedReader(new java.io.FileReader(csvPath))) {
            reader.readLine(); // Saltar el encabezado
            processLines(reader, timeThreshold, stats);
        } catch (Exception e) {
            System.err.println("Error leyendo CSV: " + e.getMessage());
        }
        return stats;
    }

    private void processLines(java.io.BufferedReader reader, long timeThreshold, RecommendationStats stats) throws java.io.IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.split(",").length == 4) {
                processLine(line, timeThreshold, stats);
            }
        }
    }

    private void processLine(String line, long timeThreshold, RecommendationStats stats) {
        String[] parts = line.split(",");
        long ts = Long.parseLong(parts[0]);
        if (ts >= timeThreshold) {
            stats.addEntry(Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3]));
        }
    }

    private String buildRecommendation(RecommendationStats stats) {
        double avgSentiment = stats.getTotalSentiment() / stats.count();
        double avgPriceDiff = stats.getTotalPriceDifference() / stats.count();
        return determineRecommendation(avgSentiment, avgPriceDiff);
    }

    private String determineRecommendation(double avgSentiment, double avgPriceDiff) {
        boolean isPriceUp = avgPriceDiff > 0;
        if (avgSentiment > -0.1 && avgSentiment < 0.1) return "Mantener";
        if (avgSentiment >= 0.1) return "Comprar";
        return isPriceUp ? "Mantener" : "Vender";
    }
}