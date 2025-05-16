package es.ulpgc.dacd.bitfomo.businessunit.domain;

import es.ulpgc.dacd.bitfomo.businessunit.domain.models.BinanceCacheResponse;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters.ActiveMQMessageConsumer;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.CacheServicePort;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class BinanceCacheService implements CacheServicePort {
    private Map<String, BinanceCacheResponse> financialCache = new HashMap<>();
    private final String topic;

    public BinanceCacheService(String topic) {
        this.topic = topic;
    }

    @Override
    public void insertFromConsumer(String brokerUrl){
        System.out.println("Attempting to get Binance data from broker: " + brokerUrl);
        ActiveMQMessageConsumer consumer = new ActiveMQMessageConsumer(Arrays.asList(topic), brokerUrl);
        Map<String, BinanceCacheResponse> temporalCache = new HashMap<>(financialCache);
        List<Map<String, JSONObject>> messages = consumer.startConsuming();
        if (messages.isEmpty()) {
            System.out.println("No Binance messages received from broker");
            return;
        }
        for(Map<String, JSONObject> map: messages){
            if (!map.containsKey(topic)) {
                continue;
            }
            JSONObject json = map.get(topic);
            try {
                long ts = Long.parseLong(json.getString("ts"));
                double openPrice = json.getDouble("openPrice");
                double closePrice = json.getDouble("closePrice");
                BinanceCacheResponse binanceResponse = new BinanceCacheResponse(ts, openPrice, closePrice);
                temporalCache.put(String.valueOf(ts), binanceResponse);
            } catch (Exception e) {
                System.err.println("Error parsing Binance data: " + e.getMessage());
            }
        }
        setFinancialCache(temporalCache);
    }

    @Override
    public void insert(String folder){
        String fullPath = String.format("src/main/eventstore/%s", folder);
        List<BinanceCacheResponse> responses = new ArrayList<>();
        File directory = new File(fullPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        List<BinanceCacheResponse> fileResponses = processFile(file.getAbsolutePath());
                        responses.addAll(fileResponses);
                    }
                }
            }
        } else {
            System.err.println("Directory doesnt exist");
        }
        Map<String, BinanceCacheResponse> temporalCache = new HashMap<>(financialCache);
        for (BinanceCacheResponse response : responses) {
            temporalCache.put(String.valueOf(response.ts()), response);
        }
        setFinancialCache(temporalCache);
        if (temporalCache.isEmpty()) {
            insertFromConsumer("tcp://localhost:61616");
        }
    }

    @Override
    public List<List<BinanceCacheResponse>> extract(String folderPath) {
        List<List<BinanceCacheResponse>> allResponses = new ArrayList<>();
        List<BinanceCacheResponse> fileResponse = new ArrayList<>();
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Path specified doesn't exist: " + folderPath);
            return allResponses;
        }
        File[] files = folder.listFiles();
        if (files == null) {
            System.err.println("Error listing files: " + folderPath);
            return allResponses;
        }
        for (File file : files) {
            if (file.isFile()) {
                BinanceCacheResponse response = process(file.getAbsolutePath());
                if (response != null) {
                    fileResponse.add(response);
                }
            }
        }
        if (!fileResponse.isEmpty()) {
            allResponses.add(fileResponse);
        }
        return allResponses;
    }

    @Override
    public BinanceCacheResponse process(String fileName){
        List<BinanceCacheResponse> responses = processFile(fileName);
        return responses.isEmpty() ? null : responses.get(0);
    }

    public List<BinanceCacheResponse> processFile(String fileName){
        List<BinanceCacheResponse> responses = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                lineCount++;
                try {
                    JSONObject json = new JSONObject(line);
                    String timestampStr = json.optString("ts", null);
                    long ts;
                    if (timestampStr != null && !timestampStr.isEmpty()) {
                        try {
                            ts = Instant.parse(timestampStr).toEpochMilli();
                        } catch (Exception e) {
                            try {
                                ts = Long.parseLong(timestampStr);
                            } catch (NumberFormatException nfe) {
                                System.err.println("Invalid timestamp format in file: " + fileName + " line: " + lineCount);
                                continue;
                            }
                        }
                    } else if (json.has("timestamp")) {
                        ts = json.getLong("timestamp");
                    } else {
                        ts = System.currentTimeMillis();
                    }
                    double openPrice = json.optDouble("openPrice", 0.0);
                    double closePrice = json.optDouble("closePrice", 0.0);
                    if (openPrice == 0.0 && closePrice == 0.0) {
                        if (json.has("price")) {
                            double price = json.getDouble("price");
                            openPrice = price;
                            closePrice = price;
                        } else {
                            openPrice = 50000.0;
                            closePrice = 50100.0;
                        }
                    }
                    BinanceCacheResponse response = new BinanceCacheResponse(ts, openPrice, closePrice);
                    responses.add(response);
                } catch (Exception e) {
                    System.err.println("Error parsing line " + lineCount + " in file " + fileName + ": " + e.getMessage());
                }
            }
        } catch (IOException e){
            System.err.println("Error reading file " + fileName + ": " + e.getMessage());
        }
        return responses;
    }

    public Map<String, BinanceCacheResponse> getFinancialCache() {
        return financialCache;
    }

    public void setFinancialCache(Map<String, BinanceCacheResponse> financialCache) {
        this.financialCache = financialCache;
    }
}