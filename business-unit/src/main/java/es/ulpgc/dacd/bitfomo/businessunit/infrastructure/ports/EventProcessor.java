package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports;

public interface EventProcessor {
    void processRedditEvent(String json);
    void processBinanceEvent(String json);
}