package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports;

public interface Deduplicator {
    boolean isDuplicate(String id);
    void register(String id);
}