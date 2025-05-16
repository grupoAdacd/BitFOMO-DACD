
package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.BusinessUnitServicePort;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.Recommendation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BusinessUnitService implements BusinessUnitServicePort {
    private final String csvPath;

    public BusinessUnitService(String csvPath) {
        this.csvPath = csvPath;
    }

    @Override
    public String generateRecommendation(String subreddit, String startTime, String endTime) {
        long startTimestamp = Instant.parse(startTime).toEpochMilli();
        long endTimestamp = Instant.parse(endTime).toEpochMilli();
        List<Recommendation> data = readDataFromCSV();
        if (data.isEmpty()) {
            return "No data available to generate recommendation.";
        }
        Recommendation earliest = null;
        long minDifferenceFromStart = Long.MAX_VALUE;
        Recommendation latest = null;
        long minDifferenceFromEnd = Long.MAX_VALUE;
        for (Recommendation rec : data) {
            if (rec.ts() >= startTimestamp && rec.ts() <= endTimestamp) {
                long diffFromStart = Math.abs(rec.ts() - startTimestamp);
                if (diffFromStart < minDifferenceFromStart) {
                    minDifferenceFromStart = diffFromStart;
                    earliest = rec;
                }
                long diffFromEnd = Math.abs(rec.ts() - endTimestamp);
                if (diffFromEnd < minDifferenceFromEnd) {
                    minDifferenceFromEnd = diffFromEnd;
                    latest = rec;
                }
            }
        }
        if (earliest == null || latest == null) {
            return "Insufficient data from the last 6 hours to generate recommendation.";
        }
        double openPrice6HoursAgo = earliest.openPrice();
        double closePriceNow = latest.closePrice();
        double percentageChange = ((closePriceNow - openPrice6HoursAgo) / openPrice6HoursAgo) * 100;
        StringBuilder result = new StringBuilder();
        result.append("=== INVESTMENT RECOMMENDATION ===\n");
        result.append(String.format("Symbol: BTC\n"));
        result.append(String.format("Time Period: Last 6 hours\n"));
        result.append(String.format("Open Price (6h ago): $%.2f\n", openPrice6HoursAgo));
        result.append(String.format("Close Price (now): $%.2f\n", closePriceNow));
        result.append(String.format("Price Change: %.2f%%\n", percentageChange));
        result.append("===========================\n");
        if (closePriceNow > openPrice6HoursAgo) {
            result.append("✅ RECOMMENDATION: INVEST\n");
            result.append(String.format("Reason: Price has increased by %.2f%% in the last 6 hours\n", percentageChange));
        } else {
            result.append("❌ RECOMMENDATION: DO NOT INVEST\n");
            result.append(String.format("Reason: Price has decreased by %.2f%% in the last 6 hours\n", Math.abs(percentageChange)));
        }
        result.append("===========================");
        return result.toString();
    }

    private List<Recommendation> readDataFromCSV() {
        List<Recommendation> recommendations = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    try {
                        long ts = Long.parseLong(parts[0].trim());
                        double openPrice = Double.parseDouble(parts[1].trim());
                        double closePrice = Double.parseDouble(parts[2].trim());
                        double sentiment = Double.parseDouble(parts[3].trim());

                        recommendations.add(new Recommendation(ts, openPrice, closePrice, sentiment));
                    } catch (NumberFormatException e) {
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
        return recommendations;
    }
}