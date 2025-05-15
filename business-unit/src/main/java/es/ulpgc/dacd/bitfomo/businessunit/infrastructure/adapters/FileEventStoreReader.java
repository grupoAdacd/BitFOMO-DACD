package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.EventStoreReaderPort;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

    public class FileEventStoreReader implements EventStoreReaderPort {
    private static final String BASE_DIR = "src/main/eventstore";

    @Override
    public List<String> readHistoricalEvents(String topic, String ss, String date) {
        List<String> events = new ArrayList<>();
        String filePath = String.format("%s/%s/%s/%s.events", BASE_DIR, topic, ss, date);

        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("No historical events found at: " + filePath);
            return events;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    events.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading historical events from " + filePath + ": " + e.getMessage());
        }

        System.out.println("Read " + events.size() + " historical events from " + filePath);
        return events;
    }
}