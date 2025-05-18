package es.ulpgc.dacd.bitfomo.businessunit.infrastructure.adapters;

import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.EventProcessor;
import es.ulpgc.dacd.bitfomo.businessunit.infrastructure.ports.EventReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class RedditFileEventReader implements EventReader {
    public void readEvents(String folderPath, EventProcessor processor) {
        File dir = new File(folderPath);
        if (dir.exists() && dir.isDirectory()) {
            processFiles(dir, processor);
        }
    }

    private void processFiles(File dir, EventProcessor processor) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    readFile(file, processor);
                }
            }
        }
    }

    private void readFile(File file, EventProcessor processor) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line, processor);
            }
        } catch (Exception e) {
            System.err.println("Error leyendo archivo de Reddit: " + e.getMessage());
        }
    }

    private void processLine(String line, EventProcessor processor) {
        String cleanedLine = line.trim();
        if (!cleanedLine.isEmpty()) {
            processor.processRedditEvent(cleanedLine);
        }
    }
}