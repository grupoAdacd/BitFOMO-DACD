package com.bitfomo.businessunit.domain;

import java.util.List;

public interface EventStoreReaderPort {
    List<String> readHistoricalEvents(String topic, String ss, String date);
}