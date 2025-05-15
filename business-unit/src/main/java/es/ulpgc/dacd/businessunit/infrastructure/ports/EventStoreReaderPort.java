package es.ulpgc.dacd.businessunit.infrastructure.ports;

import java.util.List;

public interface EventStoreReaderPort {
    List<String> readHistoricalEvents(String topic, String ss, String date);
}