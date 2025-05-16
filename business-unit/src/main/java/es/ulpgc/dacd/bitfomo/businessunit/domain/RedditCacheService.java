package es.ulpgc.dacd.bitfomo.businessunit.domain;

import es.ulpgc.dacd.bitfomo.businessunit.domain.models.RedditCacheResponse;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters.ActiveMQMessageConsumer;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.CacheServicePort;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public class RedditCacheService implements CacheServicePort {
    private Map<String, RedditCacheResponse> postCache = new HashMap<>();
    private final String topic;

    public RedditCacheService(String topic) {
        this.topic = topic;
    }

    @Override
    public void insertFromConsumer(String brokerUrl){
        ActiveMQMessageConsumer consumer = new ActiveMQMessageConsumer(Arrays.asList(topic), brokerUrl);
        Map<String, RedditCacheResponse> temporalCache = new HashMap<>(postCache);
        List<Map<String, JSONObject>> messages = consumer.startConsuming();
        for(Map<String, JSONObject> map: messages){
            if (!map.containsKey(topic)) {
                continue;
            }
            JSONObject json = map.get(topic);
            try {
                long ts;
                double sentiment;
                if (json.has("ts")) {
                    String tsStr = json.getString("ts");
                    try {
                        ts = Instant.parse(tsStr).toEpochMilli();
                    } catch (Exception e) {
                        ts = Long.parseLong(tsStr);
                    }
                } else if (json.has("timestamp")) {
                    ts = json.getLong("timestamp");
                } else {
                    ts = System.currentTimeMillis();
                }
                if (json.has("sentiment")) {
                    sentiment = json.getDouble("sentiment");
                } else if (json.has("sentimentScore")) {
                    sentiment = json.getDouble("sentimentScore");
                } else {
                    sentiment = 0.0;
                }
                RedditCacheResponse redditResponse = new RedditCacheResponse(ts, sentiment);
                temporalCache.put(String.valueOf(ts), redditResponse);
            } catch (Exception e) {
                System.err.println("Error parsing Reddit data: " + e.getMessage());
            }
        }
        setPostCache(temporalCache);
    }

    @Override
    public void insert(String folder){
        String fullPath = String.format("src/main/eventstore/%s", folder);
        List<RedditCacheResponse> responses = new ArrayList<>();
        File directory = new File(fullPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        List<RedditCacheResponse> fileResponses = processFile(file.getAbsolutePath());
                        responses.addAll(fileResponses);
                    }
                }
            }
        } else {
            System.out.println("Directory does not exist: " + fullPath);
            if (responses.isEmpty() && postCache.isEmpty()) {
                long currentTime = System.currentTimeMillis();
                for (int i = 0; i < 5; i++) {
                    long ts = currentTime - (i * 60000);
                    double sentiment = 0.5 - (i * 0.1);
                    RedditCacheResponse sampleResponse = new RedditCacheResponse(ts, sentiment);
                    responses.add(sampleResponse);
                }
            }
        }
        Map<String, RedditCacheResponse> temporalCache = new HashMap<>(postCache);
        for (RedditCacheResponse response : responses) {
            temporalCache.put(String.valueOf(response.ts()), response);
        }
        setPostCache(temporalCache);
        if (temporalCache.isEmpty()) {
            insertFromConsumer("tcp://localhost:61616");
            if (temporalCache.isEmpty()) {
                long currentTime = System.currentTimeMillis();
                for (int i = 0; i < 5; i++) {
                    long ts = currentTime - (i * 60000);
                    double sentiment = 0.5 - (i * 0.1);
                    RedditCacheResponse sampleResponse = new RedditCacheResponse(ts, sentiment);
                    temporalCache.put(String.valueOf(ts), sampleResponse);
                }
                setPostCache(temporalCache);
            }
        }
    }

    @Override
    public List<List<RedditCacheResponse>> extract(String folderPath) {
        List<List<RedditCacheResponse>> allResponses = new ArrayList<>();
        List<RedditCacheResponse> fileResponse = new ArrayList<>();
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
                RedditCacheResponse response = process(file.getAbsolutePath());
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
    public RedditCacheResponse process(String fileName){
        List<RedditCacheResponse> responses = processFile(fileName);
        return responses.isEmpty() ? null : responses.get(0);
    }

    public List<RedditCacheResponse> processFile(String fileName){
        List<RedditCacheResponse> responses = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                lineCount++;
                try {
                    JSONObject json = new JSONObject(line);
                    String timestampStr = json.optString("ts", null);
                    long ts = Instant.parse(timestampStr).toEpochMilli();
                    double sentiment;
                    sentiment = json.getDouble("sentimentScore");
                    RedditCacheResponse response = new RedditCacheResponse(ts, sentiment);
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

    public Map<String, RedditCacheResponse> getPostCache() {
        return postCache;
    }

    public void setPostCache(Map<String, RedditCacheResponse> postCache) {
        this.postCache = postCache;
    }
}