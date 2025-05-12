package com.bitfomo.adapters.broker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EventStore {
    private static final String BASE_DIR = "src/main/eventstore";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final ObjectMapper mapper = new ObjectMapper();

    public void saveEvent(String topic, String json) {
        try {
            JsonNode obj = mapper.readTree(json);
            String ss = obj.get("ss").asText();
            String ts = obj.get("ts").asText();
            LocalDate date = LocalDate.parse(ts.substring(0, 10), DateTimeFormatter.ISO_DATE);

            String dirPath = String.format("%s/%s/%s", BASE_DIR, topic, ss);
            String fileName = String.format("%s.events", date.format(DATE_FORMATTER));

            File directory = new File(dirPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File file = new File(directory, fileName);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
<<<<<<< HEAD
                System.out.println("Saving into: " + fileName);
=======
>>>>>>> feature/reddit-feeder
                writer.write(json);
                writer.newLine();
            }
        } catch (Exception e) {
<<<<<<< HEAD
            System.err.println("Error saving event: " + e.getMessage());
=======
            System.err.println("Error guardando evento: " + e.getMessage());
>>>>>>> feature/reddit-feeder
        }
    }
}
