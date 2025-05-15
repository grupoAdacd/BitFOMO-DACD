package es.ulpgc.dacd.bitfomo.businessunit.infrastructure;

import es.ulpgc.dacd.bitfomo.businessunit.domain.BinanceCacheService;
import es.ulpgc.dacd.bitfomo.businessunit.domain.CacheServiceEnsembler;
import es.ulpgc.dacd.bitfomo.businessunit.domain.RedditCacheService;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.DatamartStorerPort;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DatamartStorer implements DatamartStorerPort {
    private String csvPath;
    private final String topic1;
    private final String topic2;

    public DatamartStorer(String csvPath, String topic1, String topic2) {
        this.csvPath = csvPath;
        this.topic1 = topic1;
        this.topic2 = topic2;
    }

    @Override
    public void store() {
        System.out.println("Starting data collection and storage process...");
        BinanceCacheService binanceCacheService = new BinanceCacheService(topic1);
        RedditCacheService redditCacheService = new RedditCacheService(topic2);
        CacheServiceEnsembler ensembler = new CacheServiceEnsembler(binanceCacheService, redditCacheService);
        List<Recommendation> recommendations = ensembler.assembly();
        System.out.println("Generated " + recommendations.size() + " recommendations");
        File csvFile = new File(csvPath);
        File parentDir = csvFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        try (FileWriter writer = new FileWriter(csvPath)) {
            writer.append("ts,openPrice,closePrice,sentiment\n");

            if (recommendations.isEmpty()) {
                System.out.println("No recommendations to save to CSV");
            } else {
                for (Recommendation recommendation : recommendations) {
                    writer.append(String.valueOf(recommendation.ts()))
                            .append(",")
                            .append(String.valueOf(recommendation.openPrice()))
                            .append(",")
                            .append(String.valueOf(recommendation.closePrice()))
                            .append(",")
                            .append(String.valueOf(recommendation.sentiment()))
                            .append("\n");
                }
                System.out.println("Successfully saved " + recommendations.size() + " records to: " + csvPath);
            }

            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
            throw new RuntimeException("Error writing to CSV file: " + e.getMessage(), e);
        }
    }
}