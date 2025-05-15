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
    private Map<String, RedditCacheResponse> postCache = new HashMap<>();;
    private final String topic;
    public RedditCacheService(String topic) {
        this.topic = topic;
    }
    @Override
    public void insertFromConsumer(String brokerUrl){
        ActiveMQMessageConsumer consumer = new ActiveMQMessageConsumer(Arrays.asList(topic), brokerUrl);
        Map<String, RedditCacheResponse> temporalCache = new HashMap<>(postCache);
        for(Map<String, JSONObject> map: consumer.startConsuming()){
            RedditCacheResponse redditResponse = new RedditCacheResponse(Long.valueOf((String) map.get(topic).get("ts")), Double.valueOf((String) map.get(topic).get("sentiment")));
            temporalCache.put((String) map.get(topic).get("ts"), redditResponse);
        }
        setPostCache(temporalCache);
    }
    @Override
    public void insert(String folder){
        List<List<RedditCacheResponse>> responses = extract(String.format("src/main/eventstore/%s", folder));
        Map<String, RedditCacheResponse> temporalCache = new HashMap<>(postCache);
        for (List<RedditCacheResponse> response: responses){
            for (RedditCacheResponse object: response){
                temporalCache.put(String.valueOf(object.ts()) , object);
            }
        }
        setPostCache(temporalCache);
        // If no data was loaded from files, try consuming from broker
        if (temporalCache.isEmpty()) {
            insertFromConsumer("tcp://localhost:61616");
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
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                JSONObject json = new JSONObject(line);
                long ts = Instant.parse(json.get("ts").toString()).toEpochMilli();
                return new RedditCacheResponse(
                        ts,
                        json.getDouble("sentimentScore"));
            }
        } catch (IOException e){
            System.err.println("Error processing json line: " + e.getMessage());
        }
        return null;
    }
    public Map<String, RedditCacheResponse> getPostCache() {
        return postCache;
    }
    public void setPostCache(Map<String, RedditCacheResponse> postCache) {
        this.postCache = postCache;
    }
}