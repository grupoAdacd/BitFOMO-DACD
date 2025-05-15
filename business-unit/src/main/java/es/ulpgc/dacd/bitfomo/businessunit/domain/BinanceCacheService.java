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
        ActiveMQMessageConsumer consumer = new ActiveMQMessageConsumer(Arrays.asList(topic), brokerUrl);
        Map<String, BinanceCacheResponse> temporalCache = new HashMap<>(financialCache);
        for(Map<String, JSONObject> map: consumer.startConsuming()){
            BinanceCacheResponse binanceResponse = new BinanceCacheResponse(Long.valueOf((String) map.get(topic).get("ts")), Double.valueOf((String) map.get(topic).get("openPrice")), Double.valueOf((String) map.get(topic).get("closePrice")));
            temporalCache.put((String) map.get(topic).get("ts"), binanceResponse);
        }
        setFinancialCache(temporalCache);
    }

    @Override
    public void insert(String folder){
        List<List<BinanceCacheResponse>> responses = extract(String.format("src/main/eventstore/%s", folder));
        Map<String, BinanceCacheResponse> temporalCache = new HashMap<>(financialCache);
        for (List<BinanceCacheResponse> response: responses){
            for (BinanceCacheResponse object: response){
                temporalCache.put(String.valueOf(object.ts()) , object);
            }
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
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                JSONObject json = new JSONObject(line);
                long ts = Instant.parse(json.get("ts").toString()).toEpochMilli();
                return new BinanceCacheResponse(ts,
                        json.getDouble("openPrice"),
                        json.getDouble("closePrice"));
            }
        } catch (IOException e){
            System.err.println("Error processing json line: " + e.getMessage());
        }
        return null;
    }

    public Map<String, BinanceCacheResponse> getFinancialCache() {
        return financialCache;
    }

    public void setFinancialCache(Map<String, BinanceCacheResponse> financialCache) {
        this.financialCache = financialCache;
    }
}