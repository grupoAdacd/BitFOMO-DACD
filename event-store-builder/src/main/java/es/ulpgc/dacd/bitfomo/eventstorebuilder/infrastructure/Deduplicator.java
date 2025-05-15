package es.ulpgc.dacd.bitfomo.eventstorebuilder.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class Deduplicator {
    private static final String EVENT_STORE_BASE_DIR = "src/main/eventstore";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final ObjectMapper mapper;
    private final Set<String> redditPostIds;

    public Deduplicator() {
        this.mapper = new ObjectMapper();
        this.redditPostIds = new HashSet<>();
        loadRedditPostIds();
    }

    private void loadRedditPostIds() {
        String date = DATE_FORMATTER.format(LocalDate.now());
        String filePath = String.format("%s/%s/%s/%s.events", EVENT_STORE_BASE_DIR, "RedditPost", "reddit-feeder", date);

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("No historical events found at: " + filePath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    JsonNode event = mapper.readTree(line);
                    String postId = event.path("id").asText();
                    if (postId != null && !postId.isEmpty()) {
                        redditPostIds.add(postId);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading historical events from " + filePath + ": " + e.getMessage());
        }
    }

    public boolean isDuplicateRedditPost(String postId) {
        return redditPostIds.contains(postId);
    }

    public void addRedditPostId(String postId) {
        redditPostIds.add(postId);
    }
}