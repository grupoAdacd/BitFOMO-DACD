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
    private final String csvPath;
    private final String topic1;
    private final String topic2;
    private final BinanceCacheService binanceCacheService;
    private final RedditCacheService redditCacheService;
    private final CacheServiceEnsembler ensembler;

    public DatamartStorer(String csvPath, String topic1, String topic2) {
        this.csvPath = csvPath;
        this.topic1 = topic1;
        this.topic2 = topic2;
        this.binanceCacheService = new BinanceCacheService(topic1);
        this.redditCacheService = new RedditCacheService(topic2);
        this.ensembler = new CacheServiceEnsembler(binanceCacheService, redditCacheService);
    }

    @Override
    public void store() {
        File csvFile = new File(csvPath);
        File parentDir = csvFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.err.println("Failed to create directory structure for: " + csvPath);
            }
        }
        File eventstoreDir = new File("src/main/eventstore");
        if (!eventstoreDir.exists()) {
            if (!eventstoreDir.mkdirs()) {
                System.err.println("Failed to create eventstore directory");
            }
        }
        createDirectoryIfNotExists("src/main/eventstore/CryptoPrice/binance-feeder");
        createDirectoryIfNotExists("src/main/eventstore/RedditPost/reddit-feeder");
        List<Recommendation> recommendations = ensembler.assembly();
        boolean isNewFile = !csvFile.exists();
        try (FileWriter writer = new FileWriter(csvPath, !isNewFile)) {
            if (isNewFile) {
                writer.append("ts,openPrice,closePrice,sentiment\n");
            }
            for (Recommendation recommendation : recommendations) {
                String line = recommendation.ts() + "," +
                        recommendation.openPrice() + "," +
                        recommendation.closePrice() + "," +
                        recommendation.sentiment() + "\n";
                writer.append(line);
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error writing to CSV file: " + e.getMessage(), e);
        }
    }

    private void createDirectoryIfNotExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.err.println("Failed to create directory: " + path);
            } else {
                System.out.println("Created directory: " + path);
            }
        }
    }
}