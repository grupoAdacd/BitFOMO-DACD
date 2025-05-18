package es.ulpgc.dacd.bitfomo.businessunit.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.ulpgc.dacd.bitfomo.businessunit.domain.RedditEvent;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.Deduplicator;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.EventProcessor;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.SentimentCalculator;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class RedditEventProcessor implements EventProcessor {
    private final Deduplicator deduplicator;
    private final SentimentCalculator sentimentCalculator;
    private final ObjectMapper mapper;
    private final Map<Instant, RedditEvent> cache = new HashMap<>();

    public RedditEventProcessor(Deduplicator deduplicator, SentimentCalculator sentimentCalculator) {
        this.deduplicator = deduplicator;
        this.sentimentCalculator = sentimentCalculator;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void processRedditEvent(String json) {
        RedditEvent event = parseRedditEvent(json);
        if (event == null || deduplicator.isDuplicate(event.id())) return;
        double sentiment = calculateSentiment(event);
        RedditEvent updatedEvent = new RedditEvent(event.id(), event.ts(), event.title(), event.selftext(), sentiment);
        deduplicator.register(event.id());
        cache.put(event.ts(), updatedEvent);
    }

    @Override
    public void processBinanceEvent(String json) {
    }

    private RedditEvent parseRedditEvent(String json) {
        try {
            return mapper.readValue(json, RedditEvent.class);
        } catch (Exception e) {
            System.err.println("Error parseando evento de Reddit: " + e.getMessage());
            return null;
        }
    }

    private double calculateSentiment(RedditEvent event) {
        String text = (event.title() != null ? event.title() : "") + " " +
                (event.selftext() != null ? event.selftext() : "");
        return sentimentCalculator.calculateSentiment(text.trim());
    }

    public Map<Instant, RedditEvent> getCache() {
        return cache;
    }
}