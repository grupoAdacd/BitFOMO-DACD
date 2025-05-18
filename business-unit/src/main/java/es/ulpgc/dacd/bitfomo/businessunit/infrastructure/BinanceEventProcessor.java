package es.ulpgc.dacd.bitfomo.businessunit.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.ulpgc.dacd.bitfomo.businessunit.domain.BinanceEvent;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.EventProcessor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class BinanceEventProcessor implements EventProcessor {
    private final ObjectMapper mapper;
    private final Map<Instant, BinanceEvent> cache = new HashMap<>();

    public BinanceEventProcessor() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void processRedditEvent(String json) {
    }

    @Override
    public void processBinanceEvent(String json) {
        BinanceEvent event = parseBinanceEvent(json);
        if (event != null) {
            cache.put(event.ts(), event);
        }
    }

    private BinanceEvent parseBinanceEvent(String json) {
        try {
            return mapper.readValue(json, BinanceEvent.class);
        } catch (Exception e) {
            System.err.println("Error parseando evento de Binance: " + e.getMessage());
            return null;
        }
    }

    public Map<Instant, BinanceEvent> getCache() {
        return cache;
    }
}